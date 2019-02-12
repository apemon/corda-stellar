package com.apemon.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class ProposalContract: Contract {

    companion object {
        @JvmStatic
        val PROPOSAL_CONTRACT_ID = "com.apemon.contract.ProposalContract"
    }

    interface Commands: CommandData {
        class Issue: TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {

    }
}