package com.example.repository

import com.example.types.storage.CapsuleContents
import com.example.types.storage.TimebaseEncryptionMapper
import com.github.f4b6a3.ulid.UlidCreator
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class TimeEncryptionMapperRepository {

    suspend fun create(
        capsuleId : String,
        dataKey: String,
        timeSalt : String,
    ) : String {
        val id = UlidCreator.getUlid().toString()

        TimebaseEncryptionMapper.insert {
            it[TimebaseEncryptionMapper.capsuleId] = capsuleId
            it[TimebaseEncryptionMapper.id] = id
            it[TimebaseEncryptionMapper.timeSalt] = timeSalt
            it[TimebaseEncryptionMapper.encryptedDataKey]   = dataKey
        }

        return id
    }

    fun timeLockDataWhenOpen(capsuleId : String) : Triple<String, String, String> {
        val query = TimebaseEncryptionMapper
            .innerJoin(CapsuleContents, { TimebaseEncryptionMapper.capsuleId}, { CapsuleContents.capsuleId})
            .slice(
                TimebaseEncryptionMapper.encryptedDataKey,
                TimebaseEncryptionMapper.timeSalt,
                CapsuleContents.content,
            )
            .select { TimebaseEncryptionMapper.capsuleId eq capsuleId }

        val row = query.first()

        val key = row[TimebaseEncryptionMapper.encryptedDataKey]
        val data = row[CapsuleContents.content].toString()
        val timeSalt = row[TimebaseEncryptionMapper.timeSalt]

        return Triple(key, data, timeSalt)
    }

}