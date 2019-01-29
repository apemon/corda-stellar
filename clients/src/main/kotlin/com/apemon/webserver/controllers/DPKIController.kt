package com.apemon.webserver.controllers

import com.apemon.flow.DPKIAddFlow
import com.apemon.flow.DPKIIssueFlow
import com.apemon.flow.DPKIQueryFlow
import com.apemon.model.DPKIModel
import com.apemon.schema.DPKIModelSchemaV1
import com.apemon.state.DPKIState
import com.apemon.webserver.NodeRPCConnection
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dpki")
class DPKIController(rpc: NodeRPCConnection) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = "/get", produces = arrayOf("application/json"))
    private fun getPKI(identifier:String): DPKIModel{
        return proxy.startFlow(::DPKIQueryFlow, identifier).returnValue.get()
    }

    @PostMapping(value = "/add", produces = arrayOf("application/json"), consumes = arrayOf("application/json"))
    private fun addPKI(@RequestBody pki:DPKIModel) {
        proxy.startFlow(::DPKIAddFlow, pki).returnValue.get()
    }

    @PostMapping(value = "/issue", produces = arrayOf("application/json"), consumes = arrayOf("application/json"))
    private fun issuePKI(@RequestBody pki:DPKIModel): DPKIState {
        proxy.startFlow(::DPKIAddFlow, pki).returnValue.get()
        val state = DPKIState(owner = proxy.nodeInfo().legalIdentities.first(), keyType = pki.keyType, publicKey = pki.publicKey, network = pki.network, alias = pki.alias)
        val result = proxy.startFlow(::DPKIIssueFlow, state).returnValue.get()
        val output = result.tx.outputStates.first() as DPKIState
        return output
    }

    @GetMapping(value= "/search", produces = arrayOf("application/json"))
    private fun getDPKI(@RequestParam("network") network:String,
                        @RequestParam("name") name:String,
                        @RequestParam("owner") owner:String): List<DPKIState> {
        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
        builder {
            val ownerType = DPKIModelSchemaV1.PersistentDPKIModel::owner.equal(owner)
            val customOwnerCriteria = QueryCriteria.VaultCustomQueryCriteria(ownerType)
            val networkType = DPKIModelSchemaV1.PersistentDPKIModel::network.equal(network)
            val customNetworkCriteria = QueryCriteria.VaultCustomQueryCriteria(networkType)
            var criteria = generalCriteria.and(customOwnerCriteria).and(customNetworkCriteria)
            if (!name.isNullOrEmpty()) {
                val aliasType = DPKIModelSchemaV1.PersistentDPKIModel::alias.equal(name)
                val customAliasCriteria = QueryCriteria.VaultCustomQueryCriteria(aliasType)
                criteria = criteria.and(customAliasCriteria)
            }
            val results = proxy.vaultQueryBy<DPKIState>(criteria).states.map { it.state.data }
            return results
        }
    }
}