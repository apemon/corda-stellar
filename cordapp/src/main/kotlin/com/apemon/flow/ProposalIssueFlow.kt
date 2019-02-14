package com.apemon.flow

import co.paralleluniverse.fibers.Suspendable
import com.apemon.contract.ProposalContract
import com.apemon.service.DPKIDatabaseService
import com.apemon.state.ProposalState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Network
import org.stellar.sdk.Transaction

@InitiatingFlow
@StartableByRPC
class ProposalIssueFlow(val xdr: String,
                        val publicKey: String,
                        val participants: List<Party>,
                        val requiredSettle: Boolean = false,
                        val requiredSigner: Int,
                        val signers: List<String>):FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // retrieve private key
        Network.useTestNetwork()
        val pkiService = serviceHub.cordaService(DPKIDatabaseService::class.java)
        val pkiModel = pkiService.getPKIByPublicKey(publicKey)
        val seed = pkiModel.privateKey
        val secretKey = KeyPair.fromSecretSeed(seed)
        // sign stellar transaction
        val transaction = Transaction.fromEnvelopeXdr(xdr)
        transaction.sign(secretKey)
        // construct new proposal state
        val state = ProposalState(issuer = ourIdentity,
                participants = participants,
                status = "PROPOSE",
                originalXdr = xdr,
                requiredSettle = requiredSettle,
                candidatedSigner = signers - publicKey,
                currentSigner = listOf(publicKey),
                requiredSigner = requiredSigner,
                signedXdr = transaction.toEnvelopeXdrBase64())
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