package com.devlink.backend.controller

import com.devlink.backend.dto.AuthResponse
import com.devlink.backend.dto.LoginRequest
import com.devlink.backend.dto.LoginResponse
import com.devlink.backend.dto.RefreshTokenRequest
import com.devlink.backend.dto.VerifyOtpRequest
import com.devlink.backend.dto.ForgotPasswordRequest
import com.devlink.backend.dto.ResetPasswordRequest
import com.devlink.backend.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class AuthController(
    private val authService: AuthService
) {

    /** Step 1 of login: checks Gmail + password, emails a 6-digit OTP. Public endpoint. */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> =
        ResponseEntity.ok(authService.login(request))

    /** Step 2 of login: verifies the OTP and returns access + refresh tokens plus the role-based home route. */
    @PostMapping("/verify")
    fun verify(@Valid @RequestBody request: VerifyOtpRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.verifyOtpAndIssueTokens(request))

    /** Exchanges a valid refresh token for a new access/refresh token pair (rotation). Public endpoint. */
    @PostMapping("/refresh-token")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.refresh(request.refreshToken))

    @PostMapping("/forgot-password/request")
    fun requestForgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<Map<String, String>> {
        authService.forgotPasswordRequest(request)
        return ResponseEntity.ok(mapOf("message" to "OTP sent to ${request.email}"))
    }

    @PostMapping("/forgot-password/reset")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<Map<String, String>> {
        authService.resetPassword(request)
        return ResponseEntity.ok(mapOf("message" to "Password reset successful"))
    }
}
