package com.apemon.flow

import co.paralleluniverse.fibers.Suspendable
import com.apemon.contract.ProposalContract
import com.apemon.model.ProposalModel
import com.apemon.service.DPKIDatabaseService
import com.apemon.state.ProposalState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.finance.AMOUNT
import org.stellar.sdk.*
import org.stellar.sdk.xdr.Int64
import org.stellar.sdk.xdr.OperationType
import org.stellar.sdk.xdr.PaymentOp
import java.util.*

@InitiatingFlow
@StartableByRPC
class ProposalIssueFlow(val model: ProposalModel):FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // retrieve private key
        Network.useTestNetwork()
        val pkiService = serviceHub.cordaService(DPKIDatabaseService::class.java)
        val pkiModel = pkiService.getPKIByPublicKey(model.publicKey)
        val seed = pkiModel.privateKey
        val secretKey = KeyPair.fromSecretSeed(seed)
        // extract settlement information from transaction. Note that the transaction must contain only one payment operation.
        val transaction = Transaction.fromEnvelopeXdr(model.xdr)
        val op = transaction.operations.first().toXdr().body
        val opType = op.discriminant
        val requiredSettle = OperationType.PAYMENT == opType
        var amount: Long = 0
        var currency = "THB"
        if(requiredSettle) {
            amount = op.paymentOp.amount.int64 / 100000
            currency = String(op.paymentOp.asset.alphaNum4.assetCode).substring(0,3)
        }
        // sign stellar transaction
        transaction.sign(secretKey)
        // construct new proposal state
        var state = ProposalState(issuer = ourIdentity,
                participants = model.participants,
                originalParticipants = model.participants,
                assetIssuer = model.assetIssuer,
                assetIssuerSigner = model.assetIssuerSigner,
                status = "PROPOSE",
                originalXdr = model.xdr,
                requiredSettle = requiredSettle,
                candidatedSigner = model.signers - model.publicKey,
                currentSigner = listOf(model.publicKey),
                requiredSigner = model.requiredSigner,
                signedXdr = transaction.toEnvelopeXdrBase64(),
                settleCurrency = Currency.getInstance(currency),
                settleAmount = Amount(amount, Currency.getInstance(currency)))
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