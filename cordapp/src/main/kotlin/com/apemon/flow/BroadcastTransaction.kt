package com.apemon.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
class BroadcastTransaction(val stx: SignedTransaction,
                           val participants: List<Party>): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        var counterparties = participants - notary - ourIdentity
        val sessions = counterparties.map { initiateFlow(it)}
        sessions.forEach{ subFlow(SendTransactionFlow(it, stx))}
    }
}

@InitiatedBy(BroadcastTransaction::class)
class BroardcastTransactionResponder(val otherSession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call(){
        val flow = ReceiveTransactionFlow(
                otherSideSession = otherSession,
                checkSufficientSignatures = true,
                statesToRecord = StatesToRecord.ALL_VISIBLE
        )

        subFlow(flow)
    }
}