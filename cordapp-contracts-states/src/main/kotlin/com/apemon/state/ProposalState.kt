package com.apemon.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

data class ProposalState(val issuer: Party,
                         val status: String,
                         val requiredSettle: Boolean = false,
                         val originalXdr: String,
                         val signedXdr: String,
                         val requiredSigner: Int,
                         val candidatedSigner: List<String>,
                         val currentSigner: List<String>,
                         override val participants: List<Party> = listOf(issuer),
                         override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

}