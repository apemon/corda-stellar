package com.apemon.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class DPKIContract: Contract {

    companion object {
        @JvmStatic
        val DPKI_CONTRACT_ID = "com.apemon.contract.DPKIContract"
    }

    interface Commands: CommandData {
        class Issue: TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {

    }


}