package com.apemon.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class DPKIModel(val network: String,
                     val address: String,
                     val alias: String)