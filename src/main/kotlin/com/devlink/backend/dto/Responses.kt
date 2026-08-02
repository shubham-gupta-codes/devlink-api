package com.devlink.backend.dto

import com.devlink.backend.enums.Role
import java.time.Instant

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: Role,
    val enabled: Boolean,
    val createdAt: Instant
)

/** Returned right after /login — no tokens yet, just confirmation the OTP was sent. */
data class LoginResponse(
    val message: String,
    val email: String,
    val otpExpiryMinutes: Int
)

/**
 * Returned after successful OTP verification.
 * `homeRoute` tells the client which home screen to navigate to
 * (Student / Teacher / Recruiter / Admin), per the onboarding + role screens.
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val userId: Long,
    val username: String,
    val email: String,
    val role: Role,
    val homeRoute: String
)

data class ApiError(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String?,
    val path: String
)
