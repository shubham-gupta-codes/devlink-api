package com.devlink.backend.service.impl

import com.devlink.backend.dto.*
import com.devlink.backend.enums.Role
import com.devlink.backend.enums.VerificationStatus
import com.devlink.backend.repository.*
import com.devlink.backend.service.AdminService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminServiceImpl(
    private val userRepository: UserRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val recruiterProfileRepository: RecruiterProfileRepository,
    private val projectRepository: ProjectRepository,
    private val certificateRepository: CertificateRepository
) : AdminService {

    override fun getDashboardStats(): AdminDashboardStats {
        return AdminDashboardStats(
            totalStudents = userRepository.countByRole(Role.STUDENT),
            totalTeachers = userRepository.countByRole(Role.TEACHER),
            totalRecruiters = userRepository.countByRole(Role.RECRUITER),
            totalProjects = projectRepository.count(),
            totalVerifiedCertificates = certificateRepository.countByStatus(VerificationStatus.APPROVED),
            pendingRecruiterApprovals = recruiterProfileRepository.countByVerifiedFalse()
        )
    }

    override fun listAllUsers(role: Role?, enabled: Boolean?): List<AdminUserListItem> {
        val users = when {
            role != null && enabled != null -> userRepository.findByRoleAndEnabled(role, enabled)
            role != null -> userRepository.findByRole(role)
            enabled != null -> userRepository.findByEnabled(enabled)
            else -> userRepository.findAll()
        }
        return users.map { user ->
            val studentProfile = if (user.role == Role.STUDENT) studentProfileRepository.findByUserId(user.id!!) else null
            val teacherProfile = if (user.role == Role.TEACHER) teacherProfileRepository.findByUserId(user.id!!) else null
            val recruiterProfile = if (user.role == Role.RECRUITER) recruiterProfileRepository.findByUserId(user.id!!) else null
            AdminUserListItem(
                userId = user.id!!,
                username = user.username,
                email = user.email,
                role = user.role,
                enabled = user.enabled,
                createdAt = user.createdAt,
                displayName = studentProfile?.name ?: recruiterProfile?.companyName,
                department = teacherProfile?.department,
                companyName = recruiterProfile?.companyName,
                verified = recruiterProfile?.verified
            )
        }
    }

    @Transactional
    override fun suspendUser(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        user.enabled = false
        userRepository.save(user)
    }

    @Transactional
    override fun reactivateUser(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        user.enabled = true
        userRepository.save(user)
    }

    @Transactional
    override fun deleteUser(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        userRepository.delete(user)
    }

    override fun getPendingRecruiters(): List<PendingRecruiterResponse> {
        return recruiterProfileRepository.findByVerifiedFalse().map { profile ->
            PendingRecruiterResponse(
                userId = profile.user.id!!,
                username = profile.user.username,
                email = profile.user.email,
                companyName = profile.companyName,
                designation = profile.designation,
                contact = profile.contact,
                website = profile.website,
                joinedAt = profile.user.createdAt
            )
        }
    }

    @Transactional
    override fun approveRecruiter(userId: Long) {
        val profile = recruiterProfileRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("Recruiter profile not found: $userId")
        profile.verified = true
        recruiterProfileRepository.save(profile)
    }

    @Transactional
    override fun rejectRecruiter(userId: Long, reason: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }
        user.enabled = false
        userRepository.save(user)
    }
}
