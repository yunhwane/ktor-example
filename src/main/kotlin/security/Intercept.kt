package com.example.security

import com.example.types.GlobalResponseProvider
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineInterceptor

object Intercept {
    fun tokenHeaderVerify() : PipelineInterceptor<Unit, ApplicationCall> {
        return PipelineInterceptor@ {
            val token = call.request.headers["Authorization"].toString()

            if (token.isBlank()) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    GlobalResponseProvider.new(HttpStatusCode.Unauthorized.value,"empty token", "")
                )
                return@PipelineInterceptor finish()
            }

            try {
                PasetoProvider.verifyToken(token)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    GlobalResponseProvider.new(HttpStatusCode.Unauthorized.value,"invalid token", token)
                )
                return@PipelineInterceptor finish()
            }

            proceed()
        }
    }
}