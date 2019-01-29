package com.apemon.service

import com.apemon.model.DPKIModel
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService

val TABLE_NAME = "dpki"

@CordaService
class DPKIDatabaseService(services: ServiceHub): DatabaseService(services) {
    init {
        setupStorage()
    }

    fun insertPKI(pki: DPKIModel) {
        val query = "Insert into $TABLE_NAME values(?,?,?,?,?,?,?)"

        val params = mapOf(1 to pki.identifier,
                2 to pki.network,
                3 to pki.keyType,
                4 to pki.publicKey,
                5 to pki.privateKey,
                6 to pki.alias,
                7 to pki.description)

        executeUpdate(query, params)
        log.info("Proxy add $TABLE_NAME")
    }

    fun queryPKI(identifier: String): DPKIModel {
        val query = "select * from $TABLE_NAME where identifier = ?"
        val params = mapOf(1 to identifier)

        val results = executeQuery(query, params, {
            val iden = it.getString(1)
            val network = it.getString(2)
            val keyType = it.getString(3)
            val publicKey = it.getString(4)
            val privateKey = it.getString(5)
            val alias = it.getString(6)
            val description = it.getString(7)
            val pki = DPKIModel(identifier = iden, network = network, keyType = keyType,publicKey = publicKey, privateKey = privateKey, alias = alias, description = description)
            pki
        })

        if(results.isEmpty()) {
            throw IllegalArgumentException("$identifier is not in database.")
        }

        val value = results.single()

        return value
    }

    private fun setupStorage() {
        val query = """
            create table if not exists $TABLE_NAME(
            identifier varchar(64),
            network varchar(256),
            key_type varchar(256),
            public_key varchar(2048),
            private_key varchar(2048),
            alias varchar(2048),
            description varchar(2048),
            PRIMARY KEY (identifier))
        """

        executeUpdate(query, emptyMap())
        log.info("Create $TABLE_NAME table")
    }
}