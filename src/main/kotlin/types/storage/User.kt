package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object Users : Table(name = "users") {
    val id = char("id", 26)
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = integer("created_at")
    val updatedAt = integer("updated_at")
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}

data class UserStorage(
    val id : String,
    val email : String,
    val passwordHash : String,
    val createdAt : Int,
    val updatedAt : Int = 0,
    val isActive : Boolean
)