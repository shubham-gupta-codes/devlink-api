package com.devlink.backend.security

import com.devlink.backend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(identifier: String): UserDetails {
        val trimmed = identifier.trim()
        val user = userRepository.findByEmailIgnoreCase(trimmed)
            .orElseGet {
                userRepository.findByEmail(trimmed)
                    .orElseGet {
                        userRepository.findByUsername(trimmed)
                            .orElseThrow { UsernameNotFoundException("No user found with email or username: $trimmed") }
                    }
            }
        return UserPrincipal(user)
    }
}
