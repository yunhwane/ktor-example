package com.example

import com.example.plugins.callLogging
import com.example.plugins.configureDatabase
import com.example.plugins.contentNegotiation
import com.example.plugins.registerRouting
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    contentNegotiation()
    callLogging()
    registerRouting()
    configureDatabase()
}