package com.apemon.webserver.models

data class CashRequest(
    val to:String = "",
    val issuer: String = "PartyA",
    val amount: Int,
    val currency: String
)