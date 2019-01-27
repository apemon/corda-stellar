package com.apemon.flow

import co.paralleluniverse.fibers.Suspendable
import com.apemon.model.DPKIModel
import com.apemon.service.DPKIDatabaseService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

@InitiatingFlow
@StartableByRPC
class DPKIQueryFlow(val identifier: String): FlowLogic<DPKIModel>() {

    @Suspendable
    override fun call(): DPKIModel {
        val dpkiDatabaseService = serviceHub.cordaService(DPKIDatabaseService::class.java)
        return dpkiDatabaseService.queryPKI(identifier)
    }
}