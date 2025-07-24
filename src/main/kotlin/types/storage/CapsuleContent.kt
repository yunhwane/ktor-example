package com.example.types.storage

import org.jetbrains.exposed.sql.Table

object CapsuleContents : Table(name = "capsule_contents") {
    val id = char("id", 26)
    val capsuleId = char("capsule_id", 26).references(TimeCapsules.id)
    val contentType = enumerationByName("content_type", 10, ContentType::class)
    val content = text("content").nullable()
    val createdAt = integer("created_at")

    override val primaryKey = PrimaryKey(id)
}

enum class ContentType {
    text, image, video, audio
}