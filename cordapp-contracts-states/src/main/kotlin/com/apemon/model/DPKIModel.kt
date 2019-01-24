package com.apemon.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class DPKIModel(val owner: String,
                     val network: String,
                     val keyType: String,
                     val publicKey: String,
                     val privateKey: String,
                     val alias: String,
                     val description: String)