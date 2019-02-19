package com.apemon.webserver.controllers

import com.apemon.flow.ProposalIssueFlow
import com.apemon.state.ProposalState
import com.apemon.webserver.models.CashRequest
import com.apemon.webserver.models.ProposalRequest
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.OpaqueBytes
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.flows.CashIssueFlow
import net.corda.finance.flows.CashPaymentFlow
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/cash")
class CashController(rpc: NodeRpcConnection) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = "/all", produces = arrayOf("application/json"))
    private fun listProposal(): List<StateAndRef<Cash.State>> {
        return proxy.vaultQueryBy<Cash.State>().states
    }

    @PostMapping(value = "/issue", produces = arrayOf("application/json"))
    private fun issueCash(@RequestBody request:CashRequest): String {
        val issuedAmount = Amount(request.amount.toLong() * 100, Currency.getInstance(request.currency))
        val issuerRef = OpaqueBytes.of(0)
        val notary = proxy.notaryIdentities().first()
        val cashState = proxy.startFlow(::CashIssueFlow, issuedAmount, issuerRef, notary).returnValue.get().stx.tx.outputs.single() as Cash.State
        return cashState.toString()
    }

    @PostMapping(value = "/transfer", produces = arrayOf("application/json"))
    private fun transferCash(@RequestBody request: CashRequest): String {
        val transferAmount = Amount(request.amount.toLong() * 100, Currency.getInstance(request.currency))

        val targetParty = proxy.partiesFromName(request.to, true).first()
        val issuer = proxy.partiesFromName(request.issuer, true)
        val paymentRequest = CashPaymentFlow.PaymentRequest(transferAmount, targetParty, true, issuer)
        val cashState = proxy.startFlow(::CashPaymentFlow, paymentRequest).returnValue.get().stx.tx.outputs.single() as Cash.State
        return cashState.toString()
    }
}