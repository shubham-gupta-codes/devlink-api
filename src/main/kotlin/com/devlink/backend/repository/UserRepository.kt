package com.devlink.backend.repository

import com.devlink.backend.entity.User
import com.devlink.backend.enums.Role
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun findByEmailIgnoreCase(email: String): Optional<User>
    fun findByUsername(username: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun findByRole(role: Role): List<User>
    fun findByEnabled(enabled: Boolean): List<User>
    fun findByRoleAndEnabled(role: Role, enabled: Boolean): List<User>
    fun countByRole(role: Role): Long
}
