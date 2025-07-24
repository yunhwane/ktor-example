package com.example.plugins

import com.example.common.di.appModule
import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import com.example.common.file.FileStorage
import com.example.security.PBFDK2Provider
import com.example.security.PasetoProvider
import com.example.security.TimeBaseEncryptionProvider
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.util.Base64


fun Application.initialize() {
    pasetoInitialize(environment)
    pbfdk2Initialize(environment)
    timebaseInitialize(environment)
    minIoInitialize(environment)

    install(Koin) {
        modules(
            module { single {environment} },
            appModule
        )
    }
}
fun minIoInitialize(environment: ApplicationEnvironment) {
    try {
        val baseConfig = environment.config.config("storage")
        val bucket = baseConfig.property("bucket").getString()

        val config = baseConfig.config("minio")
        val endpoint = config.property("endpoint").getString()
        val accessKey = config.property("accessKey").getString()
        val secretKey = config.property("secretKey").getString()

        FileStorage.initialize(bucket, endpoint, accessKey, secretKey)
        environment.log.info("FileStorage initialized")
    } catch (e: Exception) {
        throw CustomException(ErrorCode.FAILED_TO_ENV, e.message)
    }
}

fun timebaseInitialize(environment: ApplicationEnvironment) {
    try {
        val securityConfig = environment.config.config("security")
        val config = securityConfig.config("timebase")

        val masetKeyBase64 = config.property("masterKey").getString()

        if (masetKeyBase64.isBlank()) {
            throw IllegalArgumentException("Master key is required")
        }

        val masterKey = Base64.getDecoder().decode(masetKeyBase64)

        if (masterKey.size != 32) {
            throw IllegalArgumentException("Master key length is not 32 bytes")
        }

        TimeBaseEncryptionProvider.initialize(masterKey)
        environment.log.info("TimeBase initialization started")
    } catch (e: Exception) {
        throw CustomException(ErrorCode.FAILED_TO_ENV, e.message)
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