package com.example.plugins

import com.example.common.di.appModule
import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.security.PBFDK2Provider
import com.example.security.PasetoProvider
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin


fun Application.initialize() {
    pasetoInitialize(environment)
    pbfdk2Initialize(environment)
    install(Koin) {
        modules(
            module { single {environment} },
            appModule
        )
    }
}


fun pbfdk2Initialize(environment: ApplicationEnvironment) {
    try {
        val securityConfig = environment.config.config("security")
        val pbfdk2Config = securityConfig.config("pbfdk2")

        val algorithm = pbfdk2Config.property("algorithm").getString()
        val iterations = pbfdk2Config.property("iterations").getString().toInt()
        val keyLength = pbfdk2Config.property("keyLength").getString().toInt()
        val saltLength = pbfdk2Config.property("saltLength").getString().toInt()

        PBFDK2Provider.initialize(algorithm, iterations, keyLength, saltLength)
        environment.log.info("pbfdk2Initialize started")
    } catch (e: Exception) {
        throw CustomException(ErrorCode.FAILED_TO_ENV, e.message)
    }
}

fun pasetoInitialize(environment: ApplicationEnvironment) {
    try {
        val securityConfig = environment.config.config("security")
        val pasetoConfig = securityConfig.config("paseto")

        val issuer = pasetoConfig.property("issuer").getString()
        val privateKey = pasetoConfig.property("privateKey").getString()
        val publicKey = pasetoConfig.property("publicKey").getString()

        PasetoProvider.initialize(issuer, privateKey, publicKey)
        environment.log.info("pasetoInitialize started")
    } catch (e : Exception) {
        throw CustomException(ErrorCode.FAILED_TO_ENV, e.message)
    }
}