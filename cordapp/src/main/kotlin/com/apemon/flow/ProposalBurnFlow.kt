package com.apemon.flow

import co.paralleluniverse.fibers.Suspendable
import com.apemon.contract.ProposalContract
import com.apemon.service.DPKIDatabaseService
import com.apemon.state.ProposalState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.OpaqueBytes
import net.corda.finance.flows.CashExitFlow
import org.intellij.lang.annotations.Flow
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Network
import org.stellar.sdk.Transaction

@InitiatingFlow
@StartableByRPC
class ProposalBurnFlow(val linearId: UniqueIdentifier,
                       val publicKey: String): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // get state
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val input = serviceHub.vaultService.queryBy<ProposalState>(queryCriteria).states.single()
        val state = input.state.data
        // check that party running this flow is issuer
        if(ourIdentity != state.assetIssuer)
            throw IllegalArgumentException("Settle flow must be initiated by the issuer")
        if(state.assetIssuerSigner != publicKey)
            throw IllegalArgumentException("publickey not match with required signers")
        // get private key & sign
        Network.useTestNetwork()
        val pkiService = serviceHub.cordaService(DPKIDatabaseService::class.java)
        val pkiModel = pkiService.getPKIByPublicKey(publicKey)
        val seed = pkiModel.privateKey
        val secret = KeyPair.fromSecretSeed(seed)
        val xdr = state.signedXdr
        val transaction = Transaction.fromEnvelopeXdr(xdr)
        transaction.sign(secret)
        val signedXdr = transaction.toEnvelopeXdrBase64()
        // build new state
        val output = state.copy(signedXdr = signedXdr,
                status = "COMPLETE",
                participants = state.originalParticipants)
        // get notary
        val notary = input.state.notary
        // create transaction builder
        val builder = TransactionBuilder(notary)
        // build command
        val command = Command(ProposalContract.Commands.Burn(), output.participants.map { it.owningKey })
        // add state and command to builder
        builder.addInputState(input)
        builder.addOutputState(output, ProposalContract.PROPOSAL_CONTRACT_ID)
        builder.addCommand(command)
        // verify
        builder.verify(serviceHub)
        // burn cash
        subFlow(CashExitFlow(state.settleAmount, OpaqueBytes.of(0)))
        // sign
        val ptx = serviceHub.signInitialTransaction(builder)
        val sessions = (output.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))
        val ftx = subFlow(FinalityFlow(stx))
        return ftx
    }
}

@InitiatedBy(ProposalBurnFlow::class)
class ProposalBurnFlowHandler(val flowSession: FlowSession): FlowLogic<Unit>() {

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