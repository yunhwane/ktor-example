package com.example.repository

import com.example.types.storage.Recipients
import com.github.f4b6a3.ulid.UlidCreator
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class RecipientsRepository {
    fun create(
        capsuleId : String,
        email : String,
    ) : String {
        val id = UlidCreator.getUlid().toString()

        Recipients.insert {
            it[Recipients.id] = id
            it[Recipients.capsuleId] = capsuleId
            it[Recipients.recipientEmail] = email
            it[hasViewed] = false
            it[notificationSent] = false
        }

        return id
    }

    fun recipientEmailByCapsuleId(capsuleId : String): String {
        val query = Recipients.slice(Recipients.recipientEmail).select { Recipients.capsuleId eq capsuleId }
        val result = query.first()[Recipients.recipientEmail]
        return result
    }

    fun updateHasViewAndNotiSendedById(capsuleId : String, hasView : Boolean, notificationSent: Boolean) {
        Recipients.update({ Recipients.capsuleId eq capsuleId }) {
            it[Recipients.notificationSent] = notificationSent
            it[Recipients.hasViewed] = hasView
        }
    }
}