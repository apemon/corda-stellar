package com.apemon.flow

import co.paralleluniverse.fibers.Suspendable
import com.apemon.contract.ProposalContract
import com.apemon.state.ProposalState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class ProposalIssueFlow(val xdr: String,
                        val participants: List<Party>,
                        val signers: List<String>):FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // construct new proposal state
        val hash = SecureHash.sha256(xdr).toString()
        val state = ProposalState(xdr = xdr,
                status = "PROPOSE",
                participants = participants,
                issuer = ourIdentity,
                signers = signers,
                hash = hash)
        // get notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // build command
        val issueCommand = Command(ProposalContract.Commands.Issue(), state.participants.map { it.owningKey })
        // build transaction
        val builder = TransactionBuilder(notary)
        builder.addCommand(issueCommand)
        builder.addOutputState(state, ProposalContract.PROPOSAL_CONTRACT_ID)
        builder.verify(serviceHub)
        // sign initial transaction
        val ptx = serviceHub.signInitialTransaction(builder)
        val sessions = (state.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))
        val ftx = subFlow(FinalityFlow(stx))
        return ftx
    }
}

@InitiatedBy(ProposalIssueFlow::class)
class ProposalIssueFlowHanlder(val flowSession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object: SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an proposal transaction" using (output is ProposalState)
                }
            }
        }
        subFlow(signedTransactionFlow)
    }
}