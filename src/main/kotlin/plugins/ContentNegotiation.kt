package com.example.plugins

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun Application.contentNegotiation() {

    val javaTimeModule = JavaTimeModule().apply {
        addSerializer(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>() {
            override fun serialize(
                value: LocalDateTime,
                gen: JsonGenerator,
                serializers: SerializerProvider,
            ) {
                gen.writeString(
                    value.truncatedTo(ChronoUnit.SECONDS).atZone(ZoneId.systemDefault()).format(
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    )
                )
            }
        })
        addDeserializer(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime>() {
            override fun deserialize(
                parser: JsonParser,
                ctxt: DeserializationContext,
            ): LocalDateTime? {
                return LocalDateTime.parse(parser.valueAsString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        })
    }

    install(ContentNegotiation) {
        jackson {
            registerModule(javaTimeModule)
            propertyNamingStrategy = SnakeCaseStrategy()
        }
    }
}