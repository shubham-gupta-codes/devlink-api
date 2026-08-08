package com.devlink.backend.dto

import com.devlink.backend.enums.*
import java.time.Instant

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: Role,
    val enabled: Boolean,
    val createdAt: Instant,
    val profile: ProfileResponse? = null
)

data class ProfileResponse(
    val userId: Long,
    val role: Role,
    val username: String,
    val email: String,

    // Student fields
    val name: String? = null,
    val photoUrl: String? = null,
    val course: Course? = null,
    val branch: Branch? = null,
    val admissionYear: Int? = null,
    val semester: Int? = null,
    val status: AcademicStatus? = null,
    val bio: String? = null,
    val totalPoints: Int? = null,
    val contact: String? = null,
    val socialLinks: List<SocialLinkResponse>? = null,

    // Teacher fields
    val employeeId: Long? = null,
    val department: String? = null,
    val designation: String? = null,

    // Recruiter fields
    val companyName: String? = null,
    val companyLogo: String? = null
)

data class SocialLinkResponse(
    val platform: SocialPlatform,
    val url: String
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

data class SkillResponse(
    val id: Long,
    val name: String
)

data class StudentSkillResponse(
    val id: Long,
    val skillName: String,
    val level: com.devlink.backend.enums.SkillLevel,
    val pointsAwarded: Int,
    val approvedByTeacherName: String?,
    val approvedAt: Instant
)

data class SkillRequestResponse(
    val id: Long,
    val studentName: String,
    val studentUsername: String,
    val skillName: String,
    val teacherName: String,
    val status: com.devlink.backend.enums.SkillStatus,
    val meetingDate: Instant?,
    val meetingNotes: String?,
    val rejectionReason: String?,
    val createdAt: Instant
)

data class NotificationResponse(
    val id: Long,
    val title: String,
    val message: String,
    val read: Boolean,
    val createdAt: Instant
)

