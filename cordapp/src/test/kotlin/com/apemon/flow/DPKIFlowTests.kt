package com.apemon.flow

import com.apemon.model.DPKIModel
import com.nhaarman.mockito_kotlin.mock
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.stellar.sdk.KeyPair
import kotlin.test.assertEquals

class DPKIFlowTests {
    lateinit var mockNetwork: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode

    @Before
    fun setup() {
        mockNetwork = MockNetwork(listOf("com.apemon"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "Bangkok", "TH"))), threadPerNode = false)
        a = mockNetwork.createNode(MockNodeParameters())
        b = mockNetwork.createNode(MockNodeParameters())
        val startedNodes = arrayListOf(a,b)
        mockNetwork.runNetwork()
    }

    @After
    fun tearDown() {
        mockNetwork.stopNodes()
    }

    @Test
    fun flowAddPKICorrectly() {
        val issuer = a.info.legalIdentities.first()
        val pair = KeyPair.random()
        val publicKey = pair.accountId
        val privateKey = String(pair.secretSeed)
        val network = "horizon-testnet"
        val identifier = SecureHash.sha256(network + ":" + publicKey).toString()
        val keyType = "ed25519 ";
        val pki = DPKIModel(identifier = identifier, network = network, keyType = keyType, privateKey = privateKey, publicKey = publicKey,description = "", alias = "")
        //a.startFlow(DPKIAddFlow(pki))
        //mockNetwork.runNetwork()
        //val future = a.startFlow(DPKIQueryFlow(identifier))
        //mockNetwork.runNetwork()
        //val result = future.getOrThrow()
        //assertEquals(publicKey, result.publicKey)
    }
}