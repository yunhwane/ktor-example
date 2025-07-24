package com.example.common.factory

import com.example.config.EmailConfig

class AWSMailService() : EmailConfig.EmailServiceImpl {

    @Throws(EmailConfig.EmailException::class)
    override fun sendEmail(to: String, subject: String, body: String) {
    }

    override fun provider() : String {
        TODO()
    }
}