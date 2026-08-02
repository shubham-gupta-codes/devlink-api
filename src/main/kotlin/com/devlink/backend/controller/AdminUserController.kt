package com.devlink.backend.controller

import com.devlink.backend.dto.CreateUserRequest
import com.devlink.backend.dto.UserResponse
import com.devlink.backend.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/user")
@PreAuthorize("hasRole('ADMIN')") // belt-and-braces on top of the SecurityConfig path rule
class AdminUserController(
    private val userService: UserService
) {

    /**
     * Creates a login account for a student / teacher / recruiter / admin.
     * Requires a valid ADMIN access token in the Authorization header.
     */
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val created = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }
}
