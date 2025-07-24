package com.example.common.factory

import com.example.config.EmailConfig
import org.slf4j.LoggerFactory

class EmailServiceFactory {

    private val logger = LoggerFactory.getLogger(EmailServiceFactory::class.java)

    fun create(config : EmailConfig.EmailConfig) : EmailConfig.EmailServiceImpl {
        logger.info("Creating EmailServiceFactory config: ${config.provider}")

        try {
            return when(config.provider) {
                EmailConfig.EmailProvider.JAKATA ->  {
                    val jmConfig = config.jakataConfig
                    JakartaMailService(
                        host = jmConfig.host,
                        port = jmConfig.port,
                        username = jmConfig.username,
                        password = jmConfig.password,
                        fromEmail = config.fromEmail,
                        fromName = config.fromName,
                    )
                }

                EmailConfig.EmailProvider.AWS ->  {
                    val config = config.awsConfig
                    AWSMailService()
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("EmailServiceFactory could not create ${config.provider}" + e.message.toString())
        }
    }


}