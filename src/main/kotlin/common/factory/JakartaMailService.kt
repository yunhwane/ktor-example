package com.example.common.factory

import com.example.config.EmailConfig
import jakarta.mail.Authenticator
import jakarta.mail.Session
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import java.util.Date
import java.util.Properties

class JakartaMailService(
    private val host : String,
    private val port : String,
    private val username : String,
    private val password : String,
    private val fromEmail : String,
    private val fromName : String,
) : EmailConfig.EmailServiceImpl {
    private val logger = LoggerFactory.getLogger(JakartaMailService::class.java)
    private val session: Session

    init {
        val properties = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port)
            put("mail.smtp.auth", "true")
            // TLS 설정
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
            put("mail.debug", "true")
        }

        session = Session.getInstance(properties, object: Authenticator() {
            override fun getPasswordAuthentication() : jakarta.mail.PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })
    }

    @Throws(EmailConfig.EmailException::class)
    override fun sendEmail(to: String, subject: String, body: String) {
        sendEmailInternal(to, subject, body)
    }

    @Throws(EmailConfig.EmailException::class)
    private fun sendEmailInternal(to: String, subject: String, body: String) {
        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(fromEmail, fromName))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            message.subject = subject
            message.sentDate = Date()
            message.setContent(body, "text/html; charset=utf-8")

            Transport.send(message)

            logger.info("Email sent to $to")
        } catch (e: Exception) {
            logger.error("Failed to send email : ${e.message}")
            throw EmailConfig.EmailException("Failed to send email : ${e.message}", e)
        }
    }

    override fun provider(): String {
        return "jakata"
    }

}
