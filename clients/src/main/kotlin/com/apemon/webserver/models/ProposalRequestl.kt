package com.apemon.webserver.models

data class ProposalRequest(
    val xdr: String,
    val signers: List<String>,
    val participants: List<String>,
    val publicKey: String,
    val requiredSettle: Boolean = false,
    val requiredSigner: Int
)