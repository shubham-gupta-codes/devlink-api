package com.devlink.backend.service.impl

import com.devlink.backend.service.EmailService
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.Properties

@Primary
@Service
class GmailEmailService(
    @Value("\${app.gmail.client-id:}") private val clientId: String,
    @Value("\${app.gmail.client-secret:}") private val clientSecret: String,
    @Value("\${app.gmail.refresh-token:}") private val refreshToken: String,
    @Value("\${app.gmail.sender:shubhamg3203068@gmail.com}") private val senderEmail: String,
    @Autowired(required = false) private val mailSender: JavaMailSender?
) : EmailService {

    private val log = LoggerFactory.getLogger(GmailEmailService::class.java)

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build()

    @Volatile
    private var cachedAccessToken: String? = null
    @Volatile
    private var tokenExpiryInstant: Instant = Instant.MIN

    @Async
    override fun sendOtpEmail(toEmail: String, otpCode: String, expiryMinutes: Int) {
        val subject = "DevLink — Your Login Verification Code"
        val body = """
            Hi,

            Your DevLink login verification code is: $otpCode

            This code expires in $expiryMinutes minutes. If you didn't request this, you can safely ignore this email.

            — Team DevLink
        """.trimIndent()

        sendEmail(toEmail, subject, body, otpCode)
    }

    @Async
    override fun sendWelcomeEmail(toEmail: String, username: String, password: String, role: String) {
        val subject = "Welcome to DevLink — Your Account Has Been Created"
        val body = """
            Hi $username,

            Your DevLink account has been created by the Admin. Here are your login credentials:

              Email:    $toEmail
              Username: $username
              Password: $password
              Role:     $role

            Please log in to the DevLink app.

            — Team DevLink
        """.trimIndent()

        sendEmail(toEmail, subject, body, null)
    }

    private fun sendEmail(toEmail: String, subject: String, body: String, otpCodeForDevLog: String?) {
        val cleanClientId = clientId.trim()
        val cleanClientSecret = clientSecret.trim()
        val cleanRefreshToken = refreshToken.trim()
        val cleanSender = senderEmail.trim().ifBlank { "shubhamg3203068@gmail.com" }

        if (cleanClientId.isNotBlank() && cleanClientSecret.isNotBlank() && cleanRefreshToken.isNotBlank()) {
            try {
                sendViaGmailApi(toEmail, cleanSender, subject, body)
                log.info("Email successfully sent via Gmail API (HTTPS) to {}", toEmail)
                return
            } catch (e: Exception) {
                log.error("Failed to send email via Gmail API to {}: {}", toEmail, e.message, e)
                if (otpCodeForDevLog != null) {
                    println("ℹ️ DEV OTP LOG: OTP for $toEmail is $otpCodeForDevLog")
                }
            }
        } else {
            log.warn("Gmail API credentials not fully configured (clientId=${cleanClientId.isNotBlank()}, clientSecret=${cleanClientSecret.isNotBlank()}, refreshToken=${cleanRefreshToken.isNotBlank()}). Checking fallback...")
        }

        // Fallback: SMTP if available
        if (mailSender != null) {
            try {
                val message = SimpleMailMessage().apply {
                    setTo(toEmail)
                    this.subject = subject
                    text = body
                }
                mailSender.send(message)
                log.info("Email successfully sent via SMTP to {}", toEmail)
                return
            } catch (e: Exception) {
                log.error("Fallback SMTP send failed to {}: {}", toEmail, e.message)
            }
        }

        if (otpCodeForDevLog != null) {
            println("ℹ️ DEV OTP LOG: OTP for $toEmail is $otpCodeForDevLog")
        }
    }

    @Synchronized
    private fun getValidAccessToken(cleanClientId: String, cleanClientSecret: String, cleanRefreshToken: String): String {
        val currentToken = cachedAccessToken
        if (currentToken != null && Instant.now().isBefore(tokenExpiryInstant.minusSeconds(60))) {
            return currentToken
        }

        val requestBody = listOf(
            "client_id" to cleanClientId,
            "client_secret" to cleanClientSecret,
            "refresh_token" to cleanRefreshToken,
            "grant_type" to "refresh_token"
        ).joinToString("&") { (k, v) ->
            URLEncoder.encode(k, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(v, StandardCharsets.UTF_8)
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://oauth2.googleapis.com/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .timeout(Duration.ofSeconds(15))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("Google OAuth token refresh failed (HTTP ${response.statusCode()}): ${response.body()}")
        }

        val responseBody = response.body()
        val tokenMatch = Regex(""""access_token"\s*:\s*"([^"]+)"""").find(responseBody)
            ?: throw IllegalStateException("No access_token found in Google OAuth response: $responseBody")
        val accessToken = tokenMatch.groupValues[1]

        val expiryMatch = Regex(""""expires_in"\s*:\s*([0-9]+)""").find(responseBody)
        val expiresInSeconds = expiryMatch?.groupValues?.get(1)?.toLongOrNull() ?: 3600L

        cachedAccessToken = accessToken
        tokenExpiryInstant = Instant.now().plusSeconds(expiresInSeconds)
        return accessToken
    }

    private fun sendViaGmailApi(toEmail: String, sender: String, subject: String, body: String) {
        val accessToken = getValidAccessToken(clientId.trim(), clientSecret.trim(), refreshToken.trim())

        val session = Session.getInstance(Properties())
        val mimeMessage = MimeMessage(session).apply {
            setFrom(InternetAddress(sender, "DevLink"))
            addRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
            this.subject = subject
            setText(body, "UTF-8")
        }

        val baos = ByteArrayOutputStream()
        mimeMessage.writeTo(baos)
        val rawEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(baos.toByteArray())

        val payload = """{"raw":"$rawEncoded"}"""

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send"))
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(20))
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("Gmail API send failed (HTTP ${response.statusCode()}): ${response.body()}")
        }
    }
}
