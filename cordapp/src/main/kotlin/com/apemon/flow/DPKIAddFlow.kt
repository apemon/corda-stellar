package com.apemon.flow

import com.apemon.model.DPKIModel
import com.apemon.service.DPKIDatabaseService
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

@InitiatingFlow
@StartableByRPC
class DPKIAddFlow(val dpki: DPKIModel): FlowLogic<Unit>() {

    override fun call() {
        val databaseService = serviceHub.cordaService(DPKIDatabaseService::class.java)
        databaseService.insertPKI(dpki)
    }
}