package com.devlink.backend.service.impl

import com.devlink.backend.service.EmailService
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class SmtpEmailService(
    private val mailSender: JavaMailSender
) : EmailService {

    private val log = LoggerFactory.getLogger(SmtpEmailService::class.java)

    @Async
    override fun sendOtpEmail(toEmail: String, otpCode: String, expiryMinutes: Int) {
        try {
            val message = SimpleMailMessage().apply {
                setTo(toEmail)
                subject = "DevLink — Your Login Verification Code"
                text = """
                    Hi,

                    Your DevLink login verification code is: $otpCode

                    This code expires in $expiryMinutes minutes. If you didn't request this, you can safely ignore this email.

                    — Team DevLink
                """.trimIndent()
            }
            mailSender.send(message)
            log.info("OTP email successfully sent to {}", toEmail)
        } catch (e: Exception) {
            log.error("Could not send OTP email to {}: {}", toEmail, e.message)
            println("ℹ️ DEV OTP LOG: OTP for $toEmail is $otpCode")
        }
    }

    @Async
    override fun sendWelcomeEmail(toEmail: String, username: String, password: String, role: String) {
        try {
            val message = SimpleMailMessage().apply {
                setTo(toEmail)
                subject = "Welcome to DevLink — Your Account Has Been Created"
                text = """
                    Hi $username,

                    Your DevLink account has been created by the Admin. Here are your login credentials:

                      Email:    $toEmail
                      Username: $username
                      Password: $password
                      Role:     $role

                    Please log in to the DevLink app.

                    — Team DevLink
                """.trimIndent()
            }
            mailSender.send(message)
            log.info("Welcome email successfully sent to {}", toEmail)
        } catch (e: Exception) {
            log.error("Could not send welcome email to {}: {}", toEmail, e.message)
            println("ℹ️ DEV CREATED USER LOG: username=$username, email=$toEmail, password=$password, role=$role")
        }
    }
}
