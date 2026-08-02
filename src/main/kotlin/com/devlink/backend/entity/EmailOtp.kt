package com.devlink.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "email_otps")
class EmailOtp(

    @Column(nullable = false)
    var email: String,

    @Column(name = "otp_code", nullable = false)
    var otpCode: String,

    @Column(name = "expired_at", nullable = false)
    var expiredAt: Instant,

    @Column(nullable = false)
    var attempts: Int = 0,

    @Column(nullable = false)
    var verified: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    fun isExpired(): Boolean = Instant.now().isAfter(expiredAt)
}
