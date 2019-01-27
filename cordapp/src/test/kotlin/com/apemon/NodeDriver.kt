package com.apemon

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.User

/**
 * Allows you to run your nodes through an IDE (as opposed to using deployNodes). Do not use in a production
 * environment.
 */
fun main(args: Array<String>) {
    val rpcUsers = listOf(User("user1", "test", permissions = setOf("ALL")))

    val param = DriverParameters(isDebug = true,
            extraCordappPackagesToScan = listOf("net.corda.finance"),
            notarySpecs = listOf(NotarySpec(CordaX500Name("Notary", "Bangkok", "TH"), false)),
            waitForAllNodesToFinish = true)

    driver(param) {
        startNode(providedName = CordaX500Name("PartyA", "Bangkok", "TH"), rpcUsers = rpcUsers).getOrThrow()
        startNode(providedName = CordaX500Name("PartyB", "Bangkok", "TH"), rpcUsers = rpcUsers).getOrThrow()
    }
}
