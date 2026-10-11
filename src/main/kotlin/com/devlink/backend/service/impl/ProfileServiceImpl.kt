package com.devlink.backend.service.impl

import com.devlink.backend.dto.*
import com.devlink.backend.entity.*
import com.devlink.backend.enums.Role
import com.devlink.backend.enums.Course
import com.devlink.backend.enums.Branch
import com.devlink.backend.exception.*
import com.devlink.backend.repository.*
import com.devlink.backend.service.ProfileService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
class ProfileServiceImpl(
    private val userRepository: UserRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val recruiterProfileRepository: RecruiterProfileRepository,
    private val socialLinkRepository: SocialLinkRepository,
    private val studentBadgeRepository: StudentBadgeRepository,
    private val teacherFeedbackRepository: TeacherFeedbackRepository,
    private val passwordEncoder: PasswordEncoder
) : ProfileService {

    override fun getProfile(userId: Long): ProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ProfileNotFoundException(userId) }
        return fetchProfileResponse(user)
    }

    override fun getProfileByUsername(username: String): ProfileResponse {
        val user = userRepository.findByUsername(username)
            .orElseThrow { ProfileNotFoundByUsernameException(username) }
        return fetchProfileResponse(user)
    }

    private fun fetchProfileResponse(user: User): ProfileResponse {
        return when (user.role) {
            Role.STUDENT -> {
                val profile = studentProfileRepository.findByUser(user)
                    .orElseGet {
                        studentProfileRepository.save(
                            StudentProfile(
                                user = user,
                                name = user.username,
                                course = Course.BTECH,
                                branch = Branch.CS,
                                admissionYear = 2024,
                                semester = 1
                            )
                        )
                    }
                val socialLinks = socialLinkRepository.findByUser(user)
                profile.toResponse(user.username, user.email, socialLinks)
            }
            Role.TEACHER -> {
                val profile = teacherProfileRepository.findByUser(user)
                    .orElseGet {
                        teacherProfileRepository.save(
                            TeacherProfile(
                                user = user,
                                employeeId = user.id!!,
                                department = "General"
                            )
                        )
                    }
                profile.toResponse(user.username, user.email)
            }
            Role.RECRUITER -> {
                val profile = recruiterProfileRepository.findByUser(user)
                    .orElseGet {
                        recruiterProfileRepository.save(
                            RecruiterProfile(
                                user = user,
                                companyName = "DevLink"
                            )
                        )
                    }
                profile.toResponse(user.username, user.email)
            }
            Role.ADMIN -> {
                ProfileResponse(
                    userId = user.id!!,
                    role = Role.ADMIN,
                    username = user.username,
                    email = user.email
                )
            }
        }
    }

    @Transactional
    override fun updateProfile(userId: Long, request: UpdateProfileRequest): ProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ProfileNotFoundException(userId) }

        // 1. Update User level credentials if requested
        if (!request.username.isNullOrBlank() && request.username != user.username) {
            if (userRepository.existsByUsername(request.username)) {
                throw UsernameAlreadyExistsException(request.username)
            }
            user.username = request.username
        }

        if (!request.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(request.password)!!
        }
        userRepository.save(user)

        // 2. Update role-specific profile fields
        when (user.role) {
            Role.STUDENT -> {
                val profile = studentProfileRepository.findByUser(user)
                    .orElseThrow { ProfileNotFoundException(userId) }

                if (request.bio != null) profile.bio = request.bio
                if (request.photoUrl != null) profile.photoUrl = request.photoUrl
                if (request.contact != null) profile.contact = request.contact

                studentProfileRepository.save(profile)

                // Update social links
                if (request.socialLinks != null) {
                    if (request.socialLinks.size > 5) {
                        throw IllegalArgumentException("Maximum 5 social links allowed")
                    }
                    // Delete old social links
                    socialLinkRepository.deleteByUser(user)
                    socialLinkRepository.flush()
                    // Insert new social links
                    val linksToSave = request.socialLinks.map {
                        SocialLink(user = user, platform = it.platform, url = it.url)
                    }
                    socialLinkRepository.saveAll(linksToSave)
                }
            }
            Role.TEACHER -> {
                throw IllegalArgumentException("Profile modifications are restricted. Contact administrator to request changes.")
            }
            Role.RECRUITER -> {
                val profile = recruiterProfileRepository.findByUser(user)
                    .orElseThrow { ProfileNotFoundException(userId) }

                if (request.companyName != null) profile.companyName = request.companyName
                if (request.companyLogo != null) profile.companyLogo = request.companyLogo
                if (request.designation != null) profile.designation = request.designation
                if (request.contact != null) profile.contact = request.contact
                if (request.about != null) profile.about = request.about
                if (request.website != null) profile.website = request.website

                recruiterProfileRepository.save(profile)
            }
            Role.ADMIN -> {
                throw ProfileNotFoundException(userId)
            }
        }

        return fetchProfileResponse(user)
    }

    // Helper functions for mapping
    private fun StudentProfile.toResponse(username: String, email: String, socialLinks: List<SocialLink>): ProfileResponse {
        val allStudents = studentProfileRepository.findAll()
            .sortedByDescending { it.totalPoints ?: 0 }
        val rankPosition = allStudents.indexOfFirst { it.id == this.id } + 1
        
        val badgesList = studentBadgeRepository.findByStudent(this).map { it.badgeName }
        
        val feedbacks = teacherFeedbackRepository.findByStudentOrderByCreatedAtDesc(this)
        val latestFb = feedbacks.firstOrNull()?.let { fb ->
            TeacherFeedbackResponse(
                id = fb.id!!,
                teacherUsername = fb.teacher.user.username,
                teacherName = fb.teacher.user.username,
                department = fb.teacher.department,
                content = fb.content,
                createdAt = fb.createdAt
            )
        }

        return ProfileResponse(
            userId = this.id!!,
            role = Role.STUDENT,
            username = username,
            email = email,
            name = this.name,
            photoUrl = this.photoUrl,
            course = this.course,
            branch = this.branch,
            admissionYear = this.admissionYear,
            semester = this.semester,
            status = this.status,
            bio = this.bio,
            totalPoints = this.totalPoints,
            contact = this.contact,
            socialLinks = socialLinks.map { SocialLinkResponse(it.platform, it.url) },
            rank = rankPosition,
            badges = badgesList,
            latestFeedback = latestFb
        )
    }

    private fun TeacherProfile.toResponse(username: String, email: String): ProfileResponse {
        return ProfileResponse(
            userId = this.id!!,
            role = Role.TEACHER,
            username = username,
            email = email,
            employeeId = this.employeeId,
            department = this.department,
            designation = this.designation
        )
    }

    private fun RecruiterProfile.toResponse(username: String, email: String): ProfileResponse {
        return ProfileResponse(
            userId = this.id!!,
            role = Role.RECRUITER,
            username = username,
            email = email,
            companyName = this.companyName,
            companyLogo = this.companyLogo,
            designation = this.designation,
            contact = this.contact,
            about = this.about,
            website = this.website,
            verified = this.verified
        )
    }

    override fun getStudentRankings(
        course: Course?,
        branch: Branch?,
        admissionYear: Int?
    ): List<ProfileResponse> {
        var studentProfiles = studentProfileRepository.findAll()
        if (course != null) {
            studentProfiles = studentProfiles.filter { it.course == course }
        }
        if (branch != null) {
            studentProfiles = studentProfiles.filter { it.branch == branch }
        }
        if (admissionYear != null) {
            studentProfiles = studentProfiles.filter { it.admissionYear == admissionYear }
        }
        val sorted = studentProfiles.sortedByDescending { it.totalPoints ?: 0 }
        return sorted.map { student ->
            val user = student.user
            val socialLinks = socialLinkRepository.findByUser(user)
            student.toResponse(user.username, user.email, socialLinks)
        }
    }

    override fun searchStudents(
        query: String?,
        course: Course?,
        branch: Branch?,
        admissionYear: Int?,
        semester: Int?,
        skill: String?,
        badge: String?
    ): List<ProfileResponse> {
        val studentProfiles = studentProfileRepository.searchStudents(
            query = query?.trim()?.takeIf { it.isNotBlank() },
            course = course,
            branch = branch,
            admissionYear = admissionYear,
            semester = semester,
            skill = skill?.trim()?.takeIf { it.isNotBlank() },
            badge = badge?.trim()?.takeIf { it.isNotBlank() }
        )
        return studentProfiles.map { student ->
            val user = student.user
            val socialLinks = socialLinkRepository.findByUser(user)
            student.toResponse(user.username, user.email, socialLinks)
        }
    }
}
