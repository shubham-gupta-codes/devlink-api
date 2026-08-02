package com.devlink.backend.service

import com.devlink.backend.dto.CreateUserRequest
import com.devlink.backend.dto.UserResponse
import com.devlink.backend.entity.User
import com.devlink.backend.exception.EmailAlreadyExistsException
import com.devlink.backend.exception.UsernameAlreadyExistsException
import com.devlink.backend.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    /**
     * Creates a login account for a student/teacher/recruiter/admin.
     * Only reachable via /api/admin/user, which SecurityConfig locks to ROLE_ADMIN —
     * this mirrors "only admin can create user" from the spec.
     */
    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException(request.email)
        }
        if (userRepository.existsByUsername(request.username)) {
            throw UsernameAlreadyExistsException(request.username)
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password)!!,
            role = request.role,
            enabled = true
        )
        val saved = userRepository.save(user)
        return saved.toResponse()
    }

    private fun User.toResponse() = UserResponse(
        id = id!!,
        username = username,
        email = email,
        role = role,
        enabled = enabled,
        createdAt = createdAt
    )
}
