package com.apemon.webserver.models

data class ProposalRequest(
    val xdr: String,
    val signers: List<String>,
    val participants: List<String>,
    val publicKey: String,
    val requiredSigner: Int,
    val assetIssuer: String = "",
    val assetIssuerSigner: String = ""
)