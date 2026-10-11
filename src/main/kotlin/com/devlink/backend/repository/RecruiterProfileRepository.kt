package com.devlink.backend.repository

import com.devlink.backend.entity.RecruiterProfile
import com.devlink.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface RecruiterProfileRepository : JpaRepository<RecruiterProfile, Long> {
    fun findByUser(user: User): Optional<RecruiterProfile>
    fun findByUserUsername(username: String): Optional<RecruiterProfile>
    fun findByVerifiedFalse(): List<RecruiterProfile>
    fun countByVerifiedFalse(): Long
    fun findByUserId(userId: Long): RecruiterProfile?
}

