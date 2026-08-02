package com.devlink.backend.repository

import com.devlink.backend.entity.RefreshToken
import com.devlink.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>

    @Modifying
    @Transactional
    @Query("update RefreshToken r set r.revoked = true where r.user = :user and r.revoked = false")
    fun revokeAllActiveTokensForUser(user: User)
}
