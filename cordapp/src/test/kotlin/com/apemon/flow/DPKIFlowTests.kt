package com.apemon.flow

import com.apemon.model.DPKIModel
import com.apemon.state.DPKIState
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
    lateinit var pair: KeyPair
    lateinit var pki: DPKIModel

    @Before
    fun setup() {
        mockNetwork = MockNetwork(listOf("com.apemon"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "Bangkok", "TH"))), threadPerNode = false)
        a = mockNetwork.createNode(MockNodeParameters())
        b = mockNetwork.createNode(MockNodeParameters())
        val startedNodes = arrayListOf(a,b)
        pair = KeyPair.random()
        val publicKey = pair.accountId
        val privateKey = String(pair.secretSeed)
        val network = "horizon-testnet"
        val identifier = SecureHash.sha256(network + ":" + publicKey).toString()
        val keyType = "ed25519";
        pki = DPKIModel(identifier = identifier, network = network, keyType = keyType, privateKey = privateKey, publicKey = publicKey,description = "", alias = "")
        mockNetwork.runNetwork()
    }

    @After
    fun tearDown() {
        mockNetwork.stopNodes()
    }

    @Test
    fun flowAddPKICorrectly() {
        a.startFlow(DPKIAddFlow(pki))
        mockNetwork.runNetwork()
        val future = a.startFlow(DPKIQueryFlow(pki.identifier))
        mockNetwork.runNetwork()
        val result = future.getOrThrow()
        assertEquals(pki.publicKey, result.publicKey)
    }

    @Test
    fun flowIssuePKICorrectly() {
        val state = DPKIState(network = pki.network, owner = a.info.legalIdentities.first(), publicKey = pki.publicKey, keyType = pki.keyType, alias = pki.alias)
        val future = a.startFlow(DPKIIssueFlow(state))
        mockNetwork.runNetwork()
        val result = future.getOrThrow()
    }
}