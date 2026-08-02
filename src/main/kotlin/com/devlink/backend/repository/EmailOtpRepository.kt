package com.devlink.backend.repository

import com.devlink.backend.entity.EmailOtp
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface EmailOtpRepository : JpaRepository<EmailOtp, Long> {
    /** Most recent OTP issued for an email — verification always checks against this one. */
    fun findTopByEmailOrderByIdDesc(email: String): Optional<EmailOtp>
}
