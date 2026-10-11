package com.devlink.backend.config

import com.devlink.backend.entity.User
import com.devlink.backend.enums.Role
import com.devlink.backend.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminSeeder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.bootstrap-admin.email}") private val adminEmail: String,
    @Value("\${app.bootstrap-admin.username}") private val adminUsername: String,
    @Value("\${app.bootstrap-admin.password}") private val adminPassword: String
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(AdminSeeder::class.java)

    override fun run(vararg args: String) {
        // Ensure any existing shubham/shubhamg3203068 account has Role.ADMIN & is enabled
        val adminUsernames = listOf(adminUsername, "shubhamg3203068", "shubham")
        val adminEmails = listOf(adminEmail, "shubhamg3203068@gmail.com")

        userRepository.findAll().forEach { user ->
            if (user.username in adminUsernames || user.email in adminEmails) {
                if (user.role != Role.ADMIN || !user.enabled) {
                    user.role = Role.ADMIN
                    user.enabled = true
                    user.password = passwordEncoder.encode("1234")!!
                    userRepository.save(user)
                    log.info("Updated existing user @${user.username} (${user.email}) to Role.ADMIN")
                }
            }
        }

        // Seed default admin if missing
        if (!userRepository.existsByEmail(adminEmail) && !userRepository.existsByUsername("shubhamg3203068")) {
            userRepository.save(
                User(
                    username = "shubhamg3203068",
                    email = adminEmail,
                    password = passwordEncoder.encode("1234")!!,
                    role = Role.ADMIN,
                    enabled = true
                )
            )
            log.warn("Seeded default ADMIN shubhamg3203068 ($adminEmail)")
        }
    }
}
