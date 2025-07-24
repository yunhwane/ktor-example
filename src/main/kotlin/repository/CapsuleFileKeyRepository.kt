package com.example.repository

import com.example.types.storage.CapsuleFileKeyMapper
import com.github.f4b6a3.ulid.UlidCreator
import org.jetbrains.exposed.sql.insert

class CapsuleFileKeyRepository {

    fun create(
        capsuleId : String,
        filePath : String,
        fileName : String
    ) : String {
        val id = UlidCreator.getUlid().toString()

        CapsuleFileKeyMapper.insert {
            it[CapsuleFileKeyMapper.id] = id
            it[CapsuleFileKeyMapper.capsuleId] = capsuleId
            it[CapsuleFileKeyMapper.filePath] = filePath
            it[CapsuleFileKeyMapper.storage] = "minIO"
            it[CapsuleFileKeyMapper.fileName] = fileName
        }

        return id
    }
}