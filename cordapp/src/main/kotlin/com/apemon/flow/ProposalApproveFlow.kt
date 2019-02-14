package com.apemon.flow

import co.paralleluniverse.fibers.Suspendable
import com.apemon.contract.ProposalContract
import com.apemon.service.DPKIDatabaseService
import com.apemon.state.ProposalState
import net.corda.core.contracts.Command
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Network
import org.stellar.sdk.Transaction

@InitiatingFlow
@StartableByRPC
class ProposalApproveFlow(val linearId: UniqueIdentifier,
                          val publicKey: String): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // get state
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val input = serviceHub.vaultService.queryBy<ProposalState>(queryCriteria).states.single()
        val state = input.state.data
        // check that public key match with the ramaining signers
        if(!state.candidatedSigner.contains(publicKey))
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
        var status = state.status
        if(state.currentSigner.size == state.requiredSigner - 1)
            if(state.requiredSettle)
                status = "APPROVED"
            else
                status = "COMPLETE"
        val output = state.copy(signedXdr = signedXdr,
                candidatedSigner = state.candidatedSigner - publicKey,
                currentSigner = state.currentSigner + publicKey,
                status = status)
        // get notary
        val notary = input.state.notary
        // create transaction builder
        val builder = TransactionBuilder(notary)
        // build command
        val command = Command(ProposalContract.Commands.Approve(), output.participants.map { it.owningKey })
        // add state and command to builder
        builder.addInputState(input)
        builder.addOutputState(output, ProposalContract.PROPOSAL_CONTRACT_ID)
        builder.addCommand(command)
        // verify
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)
        val sessions = (state.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))
        val ftx = subFlow(FinalityFlow(stx))
        return ftx
    }
}

@InitiatedBy(ProposalApproveFlow::class)
class ProposalApproveFlowHanlder(val flowSession: FlowSession): FlowLogic<Unit>() {

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