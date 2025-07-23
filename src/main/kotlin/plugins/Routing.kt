package com.example.plugins

import com.example.router.auth.route.AuthRouter
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.registerRouting() {

    routing {

        get("/health-check") {
            call.respondText("OK", contentType = ContentType.Text.Plain)
        }

        route("/api/v1") {
            AuthRouter()
        }

    }
}