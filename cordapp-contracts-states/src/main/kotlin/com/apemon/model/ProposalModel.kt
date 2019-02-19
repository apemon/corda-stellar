package com.apemon.model

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class ProposalModel(val xdr: String,
                         val publicKey: String,
                         val assetIssuerSigner: String = "",
                         val assetIssuer: Party,
                         val participants: List<Party>,
                         val requiredSigner: Int,
                         val signers: List<String>)