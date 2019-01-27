package com.apemon.flow

import co.paralleluniverse.fibers.Suspendable
import com.apemon.contract.DPKIContract
import com.apemon.state.DPKIState
import net.corda.core.contracts.Command
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.apache.logging.log4j.core.tools.picocli.CommandLine

@InitiatingFlow
@StartableByRPC
class DPKIIssueFlow(val state:DPKIState): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val issueCommand = Command(DPKIContract.Commands.Issue(), state.participants.map { it.owningKey })
        val builder = TransactionBuilder(notary = notary)
        builder.addOutputState(state, DPKIContract.DPKI_CONTRACT_ID)
        builder.addCommand(issueCommand)
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)
        val ftx = subFlow(FinalityFlow(ptx))
        subFlow(BroadcastTransaction(ftx))
        return ftx
    }
}