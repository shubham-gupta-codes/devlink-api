package com.devlink.backend.service

import com.devlink.backend.dto.*
import com.devlink.backend.entity.*
import com.devlink.backend.enums.Role
import com.devlink.backend.exception.EmailAlreadyExistsException
import com.devlink.backend.exception.UsernameAlreadyExistsException
import com.devlink.backend.repository.*
import com.devlink.backend.service.EmailService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val recruiterProfileRepository: RecruiterProfileRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService
) {

    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException(request.email)
        }
        if (userRepository.existsByUsername(request.username)) {
            throw UsernameAlreadyExistsException(request.username)
        }

        // Role specific profile validation
        if (request.role != Role.ADMIN) {
            when (request.role) {
                Role.STUDENT -> {
                    if (request.name.isNullOrBlank()) throw IllegalArgumentException("Name is required for Student")
                    if (request.course == null) throw IllegalArgumentException("Course is required for Student")
                    if (request.branch == null) throw IllegalArgumentException("Branch is required for Student")
                    if (request.admissionYear == null) throw IllegalArgumentException("Admission Year is required for Student")
                    if (request.semester == null) throw IllegalArgumentException("Semester is required for Student")
                }
                Role.TEACHER -> {
                    if (request.employeeId == null) throw IllegalArgumentException("Employee ID is required for Teacher")
                    if (request.department.isNullOrBlank()) throw IllegalArgumentException("Department is required for Teacher")
                }
                Role.RECRUITER -> {
                    if (request.companyName.isNullOrBlank()) throw IllegalArgumentException("Company Name is required for Recruiter")
                }
                else -> {}
            }
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password)!!,
            role = request.role,
            enabled = true
        )
        val savedUser = userRepository.save(user)

        // Send welcome email with credentials to the new user's Gmail asynchronously
        emailService.sendWelcomeEmail(
            toEmail = request.email,
            username = request.username,
            password = request.password,   // plain-text, before hashing
            role = request.role.name
        )

        var profileResponse: ProfileResponse? = null

        // Create Profile based on role
        if (request.role != Role.ADMIN) {
            when (request.role) {
                Role.STUDENT -> {
                    val studentProfile = StudentProfile(
                        user = savedUser,
                        name = request.name!!,
                        course = request.course!!,
                        branch = request.branch!!,
                        admissionYear = request.admissionYear!!,
                        semester = request.semester!!,
                        bio = request.bio,
                        contact = request.contact,
                        totalPoints = 0
                    )
                    val savedProfile = studentProfileRepository.save(studentProfile)
                    profileResponse = ProfileResponse(
                        userId = savedUser.id!!,
                        role = Role.STUDENT,
                        username = savedUser.username,
                        email = savedUser.email,
                        name = savedProfile.name,
                        course = savedProfile.course,
                        branch = savedProfile.branch,
                        admissionYear = savedProfile.admissionYear,
                        semester = savedProfile.semester,
                        status = savedProfile.status,
                        bio = savedProfile.bio,
                        totalPoints = savedProfile.totalPoints,
                        contact = savedProfile.contact,
                        socialLinks = emptyList()
                    )
                }
                Role.TEACHER -> {
                    val teacherProfile = TeacherProfile(
                        user = savedUser,
                        employeeId = request.employeeId!!,
                        department = request.department!!,
                        designation = request.designation
                    )
                    val savedProfile = teacherProfileRepository.save(teacherProfile)
                    profileResponse = ProfileResponse(
                        userId = savedUser.id!!,
                        role = Role.TEACHER,
                        username = savedUser.username,
                        email = savedUser.email,
                        employeeId = savedProfile.employeeId,
                        department = savedProfile.department,
                        designation = savedProfile.designation
                    )
                }
                Role.RECRUITER -> {
                    val recruiterProfile = RecruiterProfile(
                        user = savedUser,
                        companyName = request.companyName!!,
                        companyLogo = request.companyLogo,
                        designation = request.designation,
                        contact = request.contact
                    )
                    val savedProfile = recruiterProfileRepository.save(recruiterProfile)
                    profileResponse = ProfileResponse(
                        userId = savedUser.id!!,
                        role = Role.RECRUITER,
                        username = savedUser.username,
                        email = savedUser.email,
                        companyName = savedProfile.companyName,
                        companyLogo = savedProfile.companyLogo,
                        designation = savedProfile.designation,
                        contact = savedProfile.contact
                    )
                }
                else -> {}
            }
        }

        return savedUser.toResponse(profileResponse)
    }

    private fun User.toResponse(profileResponse: ProfileResponse? = null) = UserResponse(
        id = id!!,
        username = username,
        email = email,
        role = role,
        enabled = enabled,
        createdAt = createdAt,
        profile = profileResponse
    )
}

