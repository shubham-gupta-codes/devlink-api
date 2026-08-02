package com.devlink.backend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtService(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.access-token-expiry-ms}") private val accessTokenExpiryMs: Long
) {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    /** Access token — short-lived, carries email (subject) + role for authorization checks. */
    fun generateAccessToken(email: String, role: String, userId: Long): String {
        val now = Date()
        val expiry = Date(now.time + accessTokenExpiryMs)
        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .claim("userId", userId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    fun extractEmail(token: String): String = extractAllClaims(token).subject

    fun extractRole(token: String): String = extractAllClaims(token)["role"] as String

    fun isTokenValid(token: String): Boolean =
        try {
            extractAllClaims(token)
            true
        } catch (ex: ExpiredJwtException) {
            false
        } catch (ex: Exception) {
            false
        }

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
