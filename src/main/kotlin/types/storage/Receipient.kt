package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object Recipients : Table(name = "recipients") {
    val id = char("id", 26)
    val capsuleId = char("capsule_id", 26).references(TimeCapsules.id)
    val recipientEmail = varchar("recipient_email", 100)
    val hasViewed = bool("has_viewed").default(false)
    val notificationSent = bool("notification_sent").default(false)

    override val primaryKey = PrimaryKey(id)
}