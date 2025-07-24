package com.example.router.capsule.route

import com.example.security.Intercept
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.CapsuleRouter() {

    route("/capsule") {
        intercept(ApplicationCallPipeline.Call, Intercept.tokenHeaderVerify())
    }
}