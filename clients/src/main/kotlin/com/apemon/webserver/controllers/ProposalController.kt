package com.apemon.webserver.controllers

import com.apemon.flow.ProposalApproveFlow
import com.apemon.flow.ProposalIssueFlow
import com.apemon.state.DPKIState
import com.apemon.state.ProposalState
import com.apemon.webserver.models.ProposalRequest
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.internal.signWithCert
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/proposal")
class ProposalController(rpc: NodeRpcConnection) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = "/all", produces = arrayOf("application/json"))
    private fun listProposal(): List<ProposalState> {
        return proxy.vaultQueryBy<ProposalState>().states.map { it.state.data }
    }

    @PostMapping(value = "/issue", produces = arrayOf("application/json"))
    private fun issueProposal(@RequestBody request: ProposalRequest): ProposalState {
        // create new proposal state
        val me = proxy.nodeInfo().legalIdentities.first()
        var participants = listOf(me)
        request.participants.forEach {
            val party = proxy.partiesFromName(it, true).first()
            participants = participants.plus(party)
        }
        val result = proxy.startFlow(::ProposalIssueFlow, request.xdr, participants, request.signers).returnValue.get()
        return result.tx.outputStates.first() as ProposalState
    }

    @PostMapping(value = "/approve/{linearId}/{publicKey}", produces = arrayOf("application/json"))
    private fun approveProposal(@PathVariable(value = "linearId") linearId:String,
                                @PathVariable(value = "publicKey") publicKey:String): ProposalState {
        val id = UniqueIdentifier.fromString(linearId)
        val result = proxy.startFlow(::ProposalApproveFlow, id, publicKey).returnValue.get()
        return result.tx.outputStates.first() as ProposalState
    }
}