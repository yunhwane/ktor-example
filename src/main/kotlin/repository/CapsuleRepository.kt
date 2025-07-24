package com.example.repository

import com.example.types.storage.CapsuleContents
import com.example.types.storage.CapsuleFileKeyMapper
import com.example.types.storage.CapsuleStatus
import com.example.types.storage.Recipients
import com.example.types.storage.TimeCapsuleByIdStorage
import com.example.types.storage.TimeCapsules
import com.github.f4b6a3.ulid.UlidCreator
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class CapsuleRepository {
    suspend fun create(
        userID : String,
        title : String,
        description : String,
        openData : Int,
        status : CapsuleStatus = CapsuleStatus.sealed
    ) : String {
        val id = UlidCreator.getUlid().toString()
        val now = (System.currentTimeMillis() / 1000).toInt()

        TimeCapsules.insert {
            it[TimeCapsules.id] = id
            it[creator_id] = userID
            it[TimeCapsules.title] = title
            it[TimeCapsules.description] = description

            it[creationDate] = now
            it[TimeCapsules.scheduledOpenDate] = openData
            it[TimeCapsules.status] = status
        }

        return id
    }

    suspend fun capsuleContnetWithRecipient(capsuleId : String) : TimeCapsuleByIdStorage?  {
        val query = TimeCapsules
            .innerJoin(CapsuleContents, { id }, {CapsuleContents.capsuleId})
            .innerJoin(Recipients, { TimeCapsules.id}, { Recipients.capsuleId})
            .leftJoin(CapsuleFileKeyMapper, { CapsuleFileKeyMapper.capsuleId}, { Recipients.capsuleId })
            .slice(
                TimeCapsules.id,
                TimeCapsules.title,
                TimeCapsules.description,
                TimeCapsules.scheduledOpenDate,
                TimeCapsules.status,

                CapsuleContents.contentType,
                CapsuleContents.content,

                Recipients.recipientEmail,
                Recipients.hasViewed,

                CapsuleFileKeyMapper.filePath,
                CapsuleFileKeyMapper.fileName,
            ).select { TimeCapsules.id eq capsuleId }

        if (query.empty()) {
            return null
        }

        val row = query.first()

        return TimeCapsuleByIdStorage(
            id = row[TimeCapsules.id],
            title = row[TimeCapsules.title],
            description = row[TimeCapsules.description],
            scheduledOpenDate = row[TimeCapsules.scheduledOpenDate],
            status = row[TimeCapsules.status].toString(),

            contentType = row[CapsuleContents.contentType].name,
            content = row[CapsuleContents.content],
            recipientEmail = row[Recipients.recipientEmail],
            hasViewed = row[Recipients.hasViewed],

            filePath = row[CapsuleFileKeyMapper.filePath],
            fileName = row[CapsuleFileKeyMapper.fileName],
        )
    }

    fun openDateByCapsuleId(capsuleId : String) : Int? {
        val query = TimeCapsules.slice(
            TimeCapsules.id,
            TimeCapsules.scheduledOpenDate,
        ).select { TimeCapsules.id.eq(capsuleId) }

        if (query.empty()) {
            return null
        }

        val row = query.first()
        return row[TimeCapsules.scheduledOpenDate]
    }

    fun updateSealStatusById(capsuleId : String, status : CapsuleStatus) {
        TimeCapsules.update( { TimeCapsules.id eq capsuleId }) {
            it[TimeCapsules.status] = status
        }
    }

}