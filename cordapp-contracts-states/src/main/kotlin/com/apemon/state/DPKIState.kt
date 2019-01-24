package com.apemon.state

import com.apemon.schema.DPKIModelSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

data class DPKIState(val network: String,
                     val keyType: String,
                     val publicKey: String,
                     val owner: Party,
                     val alias: String,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()
                     ): LinearState,QueryableState {
    override val participants: List<Party>
        get() = listOf(owner)

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(DPKIModelSchemaV1)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is DPKIModelSchemaV1 -> DPKIModelSchemaV1.PersistentDPKIModel(
                    SecureHash.sha256(this.network + ":" + this.publicKey).toString(),
                    this.network,
                    this.keyType,
                    this.publicKey,
                    this.owner.name.toString(),
                    this.alias,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

}