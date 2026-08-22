package com.devlink.backend.service

import com.devlink.backend.dto.AuthResponse
import com.devlink.backend.dto.LoginRequest
import com.devlink.backend.dto.LoginResponse
import com.devlink.backend.dto.VerifyOtpRequest
import com.devlink.backend.entity.RefreshToken
import com.devlink.backend.entity.User
import com.devlink.backend.enums.Role
import com.devlink.backend.exception.InvalidCredentialsException
import com.devlink.backend.exception.RefreshTokenInvalidException
import com.devlink.backend.exception.UserDisabledException
import com.devlink.backend.repository.RefreshTokenRepository
import com.devlink.backend.repository.UserRepository
import com.devlink.backend.security.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val authenticationManager: AuthenticationManager,
    private val otpService: OtpService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.otp.expiry-minutes}") private val otpExpiryMinutes: Int,
    @Value("\${app.jwt.refresh-token-expiry-ms}") private val refreshTokenExpiryMs: Long
) {


    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password)
            )
        } catch (ex: DisabledException) {
            throw UserDisabledException()
        } catch (ex: BadCredentialsException) {
            throw InvalidCredentialsException()
        }

        otpService.generateAndSendOtp(request.email)

        return LoginResponse(
            message = "OTP sent to ${request.email}. Please verify to continue.",
            email = request.email,
            otpExpiryMinutes = otpExpiryMinutes
        )
    }

    /** POST /api/user/verify — confirm the OTP, then issue access + refresh tokens. */
    @Transactional
    fun verifyOtpAndIssueTokens(request: VerifyOtpRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { InvalidCredentialsException() }

        otpService.verifyOtp(request.email, request.otp)

        return issueTokensFor(user)
    }

    /** POST /api/user/refresh-token — trade a still-valid refresh token for a new access token. */
    @Transactional
    fun refresh(rawRefreshToken: String): AuthResponse {
        val stored = refreshTokenRepository.findByToken(rawRefreshToken)
            .orElseThrow { RefreshTokenInvalidException() }

        if (stored.revoked || stored.isExpired()) {
            throw RefreshTokenInvalidException()
        }

        // Rotate: revoke the used token and issue a brand new pair.
        stored.revoked = true
        refreshTokenRepository.save(stored)

        return issueTokensFor(stored.user)
    }

    @Transactional
    fun forgotPasswordRequest(request: com.devlink.backend.dto.ForgotPasswordRequest) {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("No user found with email ${request.email}") }
        otpService.generateAndSendOtp(user.email)
    }

    @Transactional
    fun resetPassword(request: com.devlink.backend.dto.ResetPasswordRequest) {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("No user found with email ${request.email}") }
        otpService.verifyOtp(request.email, request.otp)
        user.password = passwordEncoder.encode(request.newPassword)!!
        userRepository.save(user)
    }

    private fun issueTokensFor(user: User): AuthResponse {
        val accessToken = jwtService.generateAccessToken(user.email, user.role.name, user.id!!)

        val refreshTokenValue = UUID.randomUUID().toString()
        val refreshToken = RefreshToken(
            user = user,
            token = refreshTokenValue,
            expiredAt = Instant.now().plus(refreshTokenExpiryMs, ChronoUnit.MILLIS)
        )
        refreshTokenRepository.save(refreshToken)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenValue,
            userId = user.id!!,
            username = user.username,
            email = user.email,
            role = user.role,
            homeRoute = homeRouteFor(user.role)
        )
    }

       private fun homeRouteFor(role: Role): String = when (role) {
        Role.STUDENT -> "/home/student"
        Role.TEACHER -> "/home/teacher"
        Role.RECRUITER -> "/home/recruiter"
        Role.ADMIN -> "/home/admin"
    }
}
