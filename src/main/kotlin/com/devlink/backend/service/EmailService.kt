package com.devlink.backend.service

interface EmailService {
    fun sendOtpEmail(toEmail: String, otpCode: String, expiryMinutes: Int)
}
