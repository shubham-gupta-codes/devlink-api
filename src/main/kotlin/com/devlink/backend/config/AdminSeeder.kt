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
