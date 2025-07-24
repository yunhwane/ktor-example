package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object TimebaseEncryptionMapper : Table(name = "time_capsule_encryption_mapper") {
    val id = char("id", 26)
    val capsuleId = char("capsule_id", 26)
    val encryptedDataKey = varchar("encrypted_data_key", 500)
    val timeSalt = varchar("time_salt", 500)

    override val primaryKey = PrimaryKey(id)
}