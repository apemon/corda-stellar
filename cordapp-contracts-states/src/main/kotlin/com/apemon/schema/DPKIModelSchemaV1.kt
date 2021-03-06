package com.apemon.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object DPKIModelSchema

object DPKIModelSchemaV1: MappedSchema(
        schemaFamily = DPKIModelSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentDPKIModel::class.java)
) {
    @Entity
    @Table(name = "dpki_states")
    class PersistentDPKIModel(
            @Column(name = "identifier")
            var identifier: String,

            @Column(name = "network")
            var network: String,

            @Column(name = "key_type")
            var keyType: String,

            @Column(name = "public_key")
            var address: String,

            @Column(name = "owner")
            var owner: String,

            @Column(name = "alias")
            var alias: String,

            @Column(name = "linear_id")
            var linearId: UUID
    ): PersistentState() {
        constructor(): this("","","","","","",UUID.randomUUID())
    }
}