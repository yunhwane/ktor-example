package com.example.repository

import com.example.types.storage.UserStorage
import com.example.types.storage.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

class UserRepository {

    suspend fun findByEmail(email : String) : UserStorage? {
        return Users.select { Users.email eq email }
            .map { rowUser(it) }
            .singleOrNull()
    }

    suspend fun existsByEmail(email : String) : Boolean {
        return Users.select { Users.email eq email }
            .limit(1)
            .count() > 0
    }



    private fun rowUser(row : ResultRow) : UserStorage {
        return UserStorage(
            id = row[Users.id],
            email = row[Users.email],
            createdAt = row[Users.createdAt],
            updatedAt = row[Users.updatedAt],
            isActive = row[Users.isActive],
            passwordHash = row[Users.passwordHash]
        )
    }

}