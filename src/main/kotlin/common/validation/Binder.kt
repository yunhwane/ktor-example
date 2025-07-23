package com.example.common.validation

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.util.toLowerCasePreservingASCIIRules
import java.lang.Exception
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestInfo(
    val name: String,
    val source : RequestSource,
    val required: Boolean = false
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Request

enum class RequestSource {
    PATH, QUERY, BODY
}


object RequestBinder {

    @PublishedApi
    internal val objectMapper = jacksonObjectMapper()

    sealed class BindResult<out T> {
        data class Success<T>(val data : T) : BindResult<T>()
        data class Error(val error : String) : BindResult<Nothing>()
    }

    suspend inline fun <reified T : Any> post(call : ApplicationCall) : BindResult<T> {
        val clazz = T::class

        if (clazz.findAnnotation<Request>() == null) {
            return BindResult.Error("can't find Request Annotation")
        }

        val body: JsonNode? = try {
            call.receiveText().let { text ->
                if (text.isNotBlank()) objectMapper.readTree(text) else null
            }
        } catch (e: Exception) {
            throw CustomException(ErrorCode.FAILED_TO_READ_BODY_REQUEST, e.message)
        }

        val constructor = clazz.constructors.first()
        val arguments = mutableMapOf<KParameter, Any?>()

        for (parameter in constructor.parameters) {
            val property = clazz.memberProperties.find { it.name == parameter.name }
            val annotation = property?.findAnnotation<RequestInfo>()

            if (annotation == null) {
                continue
            }

            val paramName = annotation.name.ifEmpty { parameter.name }

            val value = when(annotation.source) {
                RequestSource.BODY ->  {
                    body?.get(paramName)?.let {
                        convertJsonNodeToType(it, parameter.type)
                    }
                }
                else -> CustomException(ErrorCode.FAILED_TO_FIND_REQUEST_SOURCE)
            }

            if (annotation.required && value == null) {
                return BindResult.Error("failed to matching")
            }

            if (value != null || parameter.type.isMarkedNullable) {
                arguments[parameter] = value
            }
        }

        return  try {
            BindResult.Success(constructor.callBy(arguments))
        } catch (e : Exception) {
            BindResult.Error("construction error :${e.message}")
        }
    }

    suspend inline fun <reified T : Any> get(call : ApplicationCall) : BindResult<T> {
        val clazz = T::class

        if (clazz.findAnnotation<Request>() == null) {
            return BindResult.Error("can't find Request Annotation")
        }

        val constructor = clazz.constructors.first()
        val arguments = mutableMapOf<KParameter, Any?>()

        for (parameter in constructor.parameters) {
            val property = clazz.memberProperties.find { it.name == parameter.name }
            val annotation = property?.findAnnotation<RequestInfo>()

            if (annotation == null) {
                continue
            }

            val paramName = if( annotation.name.isNotEmpty()) annotation.name else parameter.name!!

            val value = when(annotation.source) {
                RequestSource.PATH -> call.parameters[paramName]
                RequestSource.QUERY -> call.request.queryParameters[paramName]
                else -> CustomException(ErrorCode.FAILED_TO_FIND_REQUEST_SOURCE)
            }

            if (annotation.required && value == null) {
                return BindResult.Error("failed to matching")
            }

            if (value != null || parameter.type.isMarkedNullable) {
                arguments[parameter] = value
            }
        }

        return  try {
            BindResult.Success(constructor.callBy(arguments))
        } catch (e : Exception) {
            BindResult.Error("construction error :${e.message}")
        }
    }

    fun convertJsonNodeToType(node: JsonNode, type: KType): Any? {
        return when {
            node.isNull -> null

            // 문자열
            type.isSubtypeOf(String::class.createType()) -> when {
                node.isNull -> ""
                else -> node.asText()
            }

            // Long 타입 - 다양한 입력 형식 처리
            type.isSubtypeOf(Long::class.createType()) -> when {
                node.isNumber -> node.asLong()
                node.isTextual -> try { node.asText().toLong() } catch (e: kotlin.Exception) { null }
                node.isBoolean -> if (node.asBoolean()) 1L else 0L
                else -> 0
            }

            // Int 타입
            type.isSubtypeOf(Int::class.createType()) -> when {
                node.isNumber -> node.asInt()
                node.isTextual -> try { node.asText().toInt() } catch (e: kotlin.Exception) { null }
                node.isBoolean -> if (node.asBoolean()) 1 else 0
                else -> 0
            }

            // Double 타입
            type.isSubtypeOf(Double::class.createType()) -> when {
                node.isNumber -> node.asDouble()
                node.isTextual -> try { node.asText().toDouble() } catch (e: kotlin.Exception) { null }
                else -> 0
            }

            // Boolean 타입
            type.isSubtypeOf(Boolean::class.createType()) -> when {
                node.isBoolean -> node.asBoolean()
                node.isTextual -> node.asText().toLowerCasePreservingASCIIRules() == "true"
                node.isNumber -> node.asInt() != 0
                else -> false
            }

            else -> null
        }
    }
}

