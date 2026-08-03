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
