package com.example.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.path
import io.ktor.util.AttributeKey
import org.slf4j.LoggerFactory
import org.slf4j.event.Level


fun Application.callLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { it.request.path().startsWith("/api") }
    }
}

val callMonitoringPlugin = createApplicationPlugin(name = "CallMonitoringPlugin") {
    val log = LoggerFactory.getLogger(this::class.java)


    /**
     * API 요청 trigger
     */
    onCall { call ->
        if (call.request.path().startsWith("/api")) {
            val start = System.currentTimeMillis() / 1000
            call.attributes.put(AttributeKey("start-time"), start)
        }
    }

    /**
     * API 응답 trigger
     */
    onCallRespond { call, body ->
        if (call.request.path().startsWith("/api")) {
            val start = call.attributes[AttributeKey("start-time")] as Long
            val duration = System.currentTimeMillis() / 1000 - start
            val path = call.request.path()

            log.info("API Status: [${call.response.status()}] [${path}] [$body] [{$duration}ms]")
        }
    }
}