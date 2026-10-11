package com.devlink.backend.service

interface EmailService {
    fun sendOtpEmail(toEmail: String, otpCode: String, expiryMinutes: Int)
    fun sendWelcomeEmail(toEmail: String, username: String, password: String, role: String)
}
