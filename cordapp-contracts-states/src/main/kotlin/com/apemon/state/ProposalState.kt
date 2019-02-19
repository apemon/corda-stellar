package com.apemon.state

import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.util.*

data class ProposalState(val issuer: Party,
                         val status: String,
                         val requiredSettle: Boolean = false,
                         val assetIssuer: Party,
                         val assetIssuerSigner: String = "",
                         val settleCurrency: Currency = Currency.getInstance("THB"),
                         val settleAmount: Amount<Currency> = Amount(0, settleCurrency),
                         val originalXdr: String,
                         val signedXdr: String,
                         val requiredSigner: Int,
                         val candidatedSigner: List<String>,
                         val currentSigner: List<String>,
                         val originalParticipants: List<Party>,
                         override val participants: List<Party> = listOf(issuer),
                         override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

}