package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object CapsuleFileKeyMapper : Table(name = "capsule_file_key_mapper") {
    val id = char("id", 26)
    val capsuleId = char("capsule_id", 26)
    val filePath = varchar("file_path", 500)
    val fileName = varchar("file_name", 500)
    val storage = varchar("storage", 500)

    override val primaryKey = PrimaryKey(id)
}
