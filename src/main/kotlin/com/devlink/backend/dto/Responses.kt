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
    val rank: Int? = null,
    val badges: List<String> = emptyList(),
    val latestFeedback: TeacherFeedbackResponse? = null,

    // Teacher fields
    val employeeId: Long? = null,
    val department: String? = null,
    val designation: String? = null,

    // Recruiter fields
    val companyName: String? = null,
    val companyLogo: String? = null,
    val about: String? = null,
    val website: String? = null,
    val verified: Boolean? = null
)

data class TeacherFeedbackResponse(
    val id: Long,
    val teacherUsername: String,
    val teacherName: String,
    val department: String,
    val content: String,
    val createdAt: Instant
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
    val createdAt: Instant,
    val studentPhotoUrl: String? = null
)

data class NotificationResponse(
    val id: Long,
    val title: String,
    val message: String,
    val read: Boolean,
    val createdAt: Instant
)

data class ProjectResponse(
    val id: Long,
    val studentUserId: Long,
    val studentUsername: String,
    val studentName: String,
    val studentPhotoUrl: String? = null,
    val name: String,
    val description: String,
    val techStack: String,
    val githubUrl: String?,
    val demoUrl: String?,
    val verificationStatus: VerificationStatus,
    val likeCount: Long,
    val commentCount: Long,
    val createdAt: Instant,
    val imageUrls: List<String>,
    val likedByCurrentUser: Boolean = false
)

data class ProjectCommentResponse(
    val id: Long,
    val projectId: Long,
    val userId: Long,
    val username: String,
    val name: String,
    val photoUrl: String? = null,
    val content: String,
    val createdAt: Instant,
    val replies: List<ProjectCommentResponse> = emptyList()
)

data class CertificateResponse(
    val id: Long,
    val studentUserId: Long,
    val studentUsername: String,
    val studentName: String,
    val studentPhotoUrl: String? = null,
    val title: String,
    val issuingOrg: String,
    val purpose: String,
    val fileUrl: String,
    val fileType: FileType,
    val verificationStatus: VerificationStatus,
    val teacherRemarks: String? = null
)

data class VerificationRequestResponse(
    val id: Long,
    val entityType: EntityType,
    val entityId: Long,
    val entityName: String,
    val requestedByUserId: Long,
    val requestedByUsername: String,
    val requestedByName: String,
    val status: VerificationStatus,
    val reviewedByUserId: Long,
    val reviewedByUsername: String,
    val reviewedByName: String,
    val reviewedAt: Instant?,
    val remarks: String?,
    val scheduleAt: Instant?,
    val projectDetails: ProjectResponse? = null,
    val certificateDetails: CertificateResponse? = null
)

data class TeacherAnalyticsResponse(
    val languageCounts: Map<String, Long>,
    val studentCountsByLanguage: List<StudentCountByLanguage>
)

data class StudentCountByLanguage(
    val language: String,
    val count: Long
)

data class JobResponse(
    val id: Long,
    val recruiterId: Long,
    val recruiterCompanyName: String,
    val recruiterCompanyLogo: String?,
    val title: String,
    val description: String,
    val jobType: com.devlink.backend.enums.JobType,
    val requiredSkills: List<String>,
    val location: String,
    val salaryRange: String?,
    val deadline: Instant,
    val startDate: Instant? = null,
    val status: com.devlink.backend.enums.JobStatus,
    val createdAt: Instant,
    val applicantCount: Long = 0,
    val hasApplied: Boolean = false
)

data class JobApplicationResponse(
    val id: Long,
    val jobId: Long,
    val jobTitle: String,
    val recruiterCompanyName: String,
    val studentProfile: ProfileResponse,
    val status: com.devlink.backend.enums.JobApplicationStatus,
    val createdAt: Instant
)

data class SavedCandidateResponse(
    val id: Long,
    val studentProfile: ProfileResponse,
    val jobId: Long?,
    val jobTitle: String?,
    val createdAt: Instant
)

data class AdminDashboardStats(
    val totalStudents: Long,
    val totalTeachers: Long,
    val totalRecruiters: Long,
    val totalProjects: Long,
    val totalVerifiedCertificates: Long,
    val pendingRecruiterApprovals: Long
)

data class AdminUserListItem(
    val userId: Long,
    val username: String,
    val email: String,
    val role: Role,
    val enabled: Boolean,
    val createdAt: Instant,
    // Role-specific display fields
    val displayName: String?,      // student name / company name
    val department: String?,       // teacher dept
    val companyName: String?,      // recruiter company
    val verified: Boolean?         // recruiter verified
)

data class PendingRecruiterResponse(
    val userId: Long,
    val username: String,
    val email: String,
    val companyName: String,
    val designation: String?,
    val contact: String?,
    val website: String?,
    val joinedAt: Instant
)



