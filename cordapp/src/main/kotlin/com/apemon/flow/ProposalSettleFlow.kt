package com.apemon.flow

import co.paralleluniverse.fibers.Suspendable
import com.apemon.contract.ProposalContract
import com.apemon.state.ProposalState
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.OpaqueBytes
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.contracts.asset.cash.selection.AbstractCashSelection
import net.corda.finance.contracts.getCashBalance
import net.corda.finance.issuedBy

@InitiatingFlow
@StartableByRPC
class ProposalSettleFlow(val linearId: UniqueIdentifier): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // get state
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val input = serviceHub.vaultService.queryBy<ProposalState>(queryCriteria).states.single()
        val state = input.state.data
        // check basic condition
        // check that party running this flow is issuer
        if(ourIdentity != state.issuer)
            throw IllegalArgumentException("Settle flow must be initiated by the issuer")
        if(!state.requiredSettle && state.settleAmount <= Amount(0, state.settleCurrency))
            throw IllegalArgumentException("Proposal don't need to settle")
        // check cash balance
        val amount = state.settleAmount
        val cashBalance = serviceHub.getCashBalance(state.settleAmount.token)
        if(cashBalance < state.settleAmount) {
            throw IllegalArgumentException("You has only $cashBalance but attempt to transfer $amount")
        }
        // create transaction builder
        val notary = input.state.notary
        val builder = TransactionBuilder(notary)
        val settleCommand = Command(ProposalContract.Commands.Settle(), listOf(ourIdentity, state.assetIssuer).map { it.owningKey })
        // generate asset exit
        val assetIssuer = state.assetIssuer
        val (_,cashKeys) = Cash.generateSpend(serviceHub, builder, state.settleAmount, ourIdentityAndCert, assetIssuer)
        // create output state
        val output = state.copy(status = "SETTLED", participants = listOf(state.issuer, state.assetIssuer))
        //
        builder.addInputState(input)
        builder.addOutputState(output, ProposalContract.PROPOSAL_CONTRACT_ID)
        builder.addCommand(settleCommand)
        // verify and sign
        builder.verify(serviceHub)
        // sign
        val myKeyToSign = (cashKeys + ourIdentity.owningKey).toList()
        val ptx = serviceHub.signInitialTransaction(builder, myKeyToSign)
        // initial session
        val counterPartySession = initiateFlow(assetIssuer)
        // send to counterParty
        subFlow(IdentitySyncFlow.Send(counterPartySession,ptx.tx))
        val stx = subFlow(CollectSignaturesFlow(ptx, listOf(counterPartySession), myOptionalKeys = myKeyToSign))
        return subFlow(FinalityFlow(stx))
    }
}

@InitiatedBy(ProposalSettleFlow::class)
class ProposalSettleFlowHandler(val flowSession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        subFlow(IdentitySyncFlow.Receive(flowSession))

        val signedTransactionFlow = object: SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {

            }
        }
        subFlow(signedTransactionFlow)
    }
}