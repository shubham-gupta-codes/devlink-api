package com.devlink.backend.service

import com.devlink.backend.entity.EmailOtp
import com.devlink.backend.exception.OtpExpiredException
import com.devlink.backend.exception.OtpInvalidException
import com.devlink.backend.exception.OtpMaxAttemptsExceededException
import com.devlink.backend.exception.OtpNotFoundException
import com.devlink.backend.repository.EmailOtpRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@Service
class OtpService(
    private val emailOtpRepository: EmailOtpRepository,
    private val emailService: EmailService,
    @Value("\${app.otp.expiry-minutes}") private val expiryMinutes: Long,
    @Value("\${app.otp.max-attempts}") private val maxAttempts: Int,
    @Value("\${app.otp.length}") private val otpLength: Int
) {


    fun generateAndSendOtp(email: String) {
        val code = randomNumericCode(otpLength)
        val otp = EmailOtp(
            email = email,
            otpCode = code,
            expiredAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        )
        emailOtpRepository.save(otp)
        emailService.sendOtpEmail(email, code, expiryMinutes.toInt())
    }


    fun verifyOtp(email: String, submittedCode: String) {
        val otp = emailOtpRepository.findTopByEmailOrderByIdDesc(email)
            .orElseThrow { OtpNotFoundException() }

        if (otp.verified) {
            return
        }

        if (otp.attempts >= maxAttempts) {
            throw OtpMaxAttemptsExceededException()
        }

        if (otp.isExpired()) {
            throw OtpExpiredException()
        }

        if (otp.otpCode != submittedCode) {
            otp.attempts += 1
            emailOtpRepository.save(otp)
            throw OtpInvalidException(maxAttempts - otp.attempts)
        }

        otp.verified = true
        emailOtpRepository.save(otp)
    }

    private fun randomNumericCode(length: Int): String {
        val bound = Math.pow(10.0, length.toDouble()).toInt()
        return Random.nextInt(0, bound).toString().padStart(length, '0')
    }
}
