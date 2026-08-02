package com.devlink.backend.dto

import com.devlink.backend.enums.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/** POST /api/admin/user — only an ADMIN can hit this. */
data class CreateUserRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 50)
    val username: String,

    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    val role: Role
)

/** POST /api/user/login — step 1: email + password check, triggers OTP email. */
data class LoginRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val password: String
)

/** POST /api/user/verify — step 2: OTP confirms it's really the account owner. */
data class VerifyOtpRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    val otp: String
)

/** POST /api/user/refresh-token — exchange a valid refresh token for a new access token. */
data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String
)
