package com.devlink.backend.exception

class EmailAlreadyExistsException(email: String) : RuntimeException("Email already registered: $email")
class UsernameAlreadyExistsException(username: String) : RuntimeException("Username already taken: $username")
class InvalidCredentialsException : RuntimeException("Invalid email or password")
class UserDisabledException : RuntimeException("This account has been disabled. Contact your administrator.")
class OtpNotFoundException : RuntimeException("No OTP request found for this email. Please login again.")
class OtpExpiredException : RuntimeException("OTP has expired. Please login again to request a new one.")
class OtpInvalidException(attemptsLeft: Int) : RuntimeException("Incorrect OTP. $attemptsLeft attempt(s) left.")
class OtpMaxAttemptsExceededException : RuntimeException("Too many incorrect attempts. Please login again to request a new OTP.")
class RefreshTokenInvalidException : RuntimeException("Refresh token is invalid, expired, or revoked")
