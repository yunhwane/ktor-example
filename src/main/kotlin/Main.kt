package com.example

import com.example.plugins.callLogging
import com.example.plugins.contentNegotiation
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.netty.EngineMain
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.time.LocalDateTime


fun main(args: Array<String>) {
    EngineMain.main(args)
}

data class Sample(
    val email: String,
    val createdAt: LocalDateTime
)

fun Application.module() {
    contentNegotiation()
    callLogging()

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/sample") {
            call.respond(Sample("naver.com", LocalDateTime.now()))
        }

        post("/sample") {
            val sample = call.receive<Sample>()
            call.respond(Sample(sample.email, LocalDateTime.now()))
        }
    }
}