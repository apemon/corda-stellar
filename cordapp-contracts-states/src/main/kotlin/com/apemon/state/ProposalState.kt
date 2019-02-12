package com.apemon.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

data class ProposalState(val issuer: Party,
                         val xdr: String,
                         val hash: String,
                         val status: String,
                         val signers: List<String>,
                         override val participants: List<Party> = listOf(issuer),
                         override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

}