package com.devlink.backend.dto

import com.devlink.backend.enums.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant

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

    val role: Role,

    // Student profile fields
    val name: String? = null,
    val course: Course? = null,
    val branch: Branch? = null,
    val admissionYear: Int? = null,
    val semester: Int? = null,
    val bio: String? = null,
    val contact: String? = null,

    // Teacher profile fields
    val employeeId: Long? = null,
    val department: String? = null,
    val designation: String? = null,

    // Recruiter profile fields
    val companyName: String? = null,
    val companyLogo: String? = null
)

data class UpdateProfileRequest(
    @field:Size(min = 3, max = 50)
    val username: String? = null,

    val password: String? = null,

    // Student profile fields
    val name: String? = null,
    val course: Course? = null,
    val branch: Branch? = null,
    val admissionYear: Int? = null,
    val semester: Int? = null,
    val bio: String? = null,
    val contact: String? = null,
    val photoUrl: String? = null,

    // Teacher profile fields
    val employeeId: Long? = null,
    val department: String? = null,
    val designation: String? = null,

    // Recruiter profile fields
    val companyName: String? = null,
    val companyLogo: String? = null,

    // Social Links
    val socialLinks: List<SocialLinkRequest>? = null
)

data class SocialLinkRequest(
    val platform: SocialPlatform,
    val url: String
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

data class SkillRequestDto(
    val skillId: Long
)

data class ApproveSkillRequest(
    val level: com.devlink.backend.enums.SkillLevel,
    val points: Int
)

data class ScheduleMeetingRequest(
    val meetingDate: java.time.Instant,
    val notes: String
)

data class RejectSkillRequest(
    val reason: String
)

data class UploadProjectRequest(
    val name: String,
    val description: String,
    val techStack: String,
    val githubUrl: String?,
    val demoUrl: String?,
    val imageUrls: List<String>? = null
)

data class ProjectCommentRequest(
    val content: String,
    val parentCommentId: Long? = null
)

data class UploadCertificateRequest(
    val title: String,
    val issuingOrg: String,
    val purpose: String,
    val fileUrl: String,
    val fileType: FileType
)

data class EvaluateVerificationRequest(
    val status: VerificationStatus,
    val remarks: String? = null,
    val points: Int? = null,
    val scheduleAt: Instant? = null
)


