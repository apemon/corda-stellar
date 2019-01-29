package com.apemon.webserver.controllers

import com.apemon.flow.DPKIAddFlow
import com.apemon.flow.DPKIQueryFlow
import com.apemon.model.DPKIModel
import com.apemon.webserver.NodeRPCConnection
import net.corda.core.messaging.startFlow
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
}