package com.example.config

import com.example.common.exception.CustomException
import com.example.common.exception.ErrorCode
import io.ktor.server.config.ApplicationConfig

class EmailConfig {
    interface EmailServiceImpl {
        @Throws(EmailException::class)
        fun sendEmail(to: String, subject: String, body: String)

        fun provider(): String
    }

    class EmailException(message: String, cause: Throwable?) : Exception(message, cause)


    enum class EmailProvider {
        JAKATA, AWS;

        companion object {
            fun fromString(v: String): EmailProvider {
                return when (v.lowercase()) {
                    "aws" -> AWS
                    else -> JAKATA
                }
            }
        }
    }

    data class EmailConfig(
        val provider: EmailProvider,
        val fromEmail: String,
        val fromName: String,
        val jakataConfig: JakataConfig,
        val awsConfig: AWSConfig
    ) {
        data class JakataConfig(
            val host: String,
            val port: String,
            val username: String,
            val password: String,
        )

        data class AWSConfig(
            val region: String,
            val accessKey: String,
            val secretKey: String,
        )

        companion object {
            fun fromApplicationConfig(cfg: ApplicationConfig): EmailConfig {
                try {

                    val emailConfig = cfg.config("email")
                    val provider = emailConfig.property("provider").getString()
                    val fromEmail = emailConfig.property("fromEmail").getString()
                    val fromName = emailConfig.property("fromName").getString()

                    val jakataSetting: JakataConfig = if (EmailProvider.fromString(provider) == EmailProvider.JAKATA) {
                        val jakataConfig = emailConfig.config("jakata")
                        JakataConfig(
                            host = jakataConfig.property("host").getString(),
                            port = jakataConfig.property("port").getString(),
                            username = jakataConfig.property("username").getString(),
                            password = jakataConfig.property("password").getString()
                        )
                    } else {
                        JakataConfig(
                            host = "",
                            port = "",
                            username = "",
                            password = ""
                        )
                    }

                    val awsSetting: AWSConfig = if (EmailProvider.fromString(provider) == EmailProvider.AWS) {
                        val awsConfig = emailConfig.config("aws")
                        AWSConfig(
                            region = awsConfig.property("region").getString(),
                            accessKey = awsConfig.property("accessKey").getString(),
                            secretKey = awsConfig.property("secretKey").getString(),
                        )
                    } else {
                        AWSConfig(
                            region = "",
                            accessKey = "",
                            secretKey = "",
                        )
                    }

                    return EmailConfig(
                        provider = EmailProvider.fromString(provider),
                        fromEmail = fromEmail,
                        fromName = fromName,
                        jakataConfig = jakataSetting,
                        awsConfig = awsSetting,
                    )
                } catch (e: Exception) {
                    throw CustomException(ErrorCode.FAILED_TO_ENV, e.message)
                }
            }
        }

    }
}