package com.example.common.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonHandler {
    private val mapper : ObjectMapper = jacksonObjectMapper().apply {
        configure(SerializationFeature.INDENT_OUTPUT, true)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        registerModule(JavaTimeModule())
    }

    fun <T> encodeToJson(data : T) : String {
        return mapper.writeValueAsString(data)
    }

    fun <T> decodeFromJson(jsonString : String, v : Class<T>): T {
        return mapper.readValue(jsonString, v)
    }

    fun encodeToBytes(v : Any) : ByteArray {
        return mapper.writeValueAsBytes(v)
    }
}