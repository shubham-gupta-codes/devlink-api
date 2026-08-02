package com.devlink.backend.config

import com.devlink.backend.entity.User
import com.devlink.backend.enums.Role
import com.devlink.backend.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * Bootstraps exactly one ADMIN account on first boot, since /api/admin/user is
 * locked to ROLE_ADMIN and there'd otherwise be no way to create the first user.
 * Change ADMIN_EMAIL / ADMIN_PASSWORD via env vars before deploying anywhere real,
 * and rotate the password after first login.
 */
@Component
class AdminSeeder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.bootstrap-admin.email:admin@devlink.com}") private val adminEmail: String,
    @Value("\${app.bootstrap-admin.username:admin}") private val adminUsername: String,
    @Value("\${app.bootstrap-admin.password:ChangeMe123!}") private val adminPassword: String
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(AdminSeeder::class.java)

    override fun run(vararg args: String) {
        if (userRepository.existsByEmail(adminEmail)) return

        userRepository.save(
            User(
                username = adminUsername,
                email = adminEmail,
                password = passwordEncoder.encode(adminPassword)!!,
                role = Role.ADMIN,
                enabled = true
            )
        )
        log.warn("Seeded default ADMIN ($adminEmail) — change this password immediately after first login.")
    }
}
