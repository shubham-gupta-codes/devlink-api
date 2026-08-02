package com.devlink.backend.service.impl

import com.devlink.backend.service.EmailService
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class SmtpEmailService(
    private val mailSender: JavaMailSender
) : EmailService {

    override fun sendOtpEmail(toEmail: String, otpCode: String, expiryMinutes: Int) {
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
    }
}
