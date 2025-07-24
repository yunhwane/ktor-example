package com.example

import com.example.common.di.appModule
import com.example.plugins.callLogging
import com.example.plugins.configureDatabase
import com.example.plugins.contentNegotiation
import com.example.plugins.registerRouting
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    contentNegotiation()
    configureDatabase()
    install(Koin) {
        modules(
            appModule
        )
    }
    callLogging()
    registerRouting()

}