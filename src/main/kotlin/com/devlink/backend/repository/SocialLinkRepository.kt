package com.devlink.backend.repository

import com.devlink.backend.entity.SocialLink
import com.devlink.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface SocialLinkRepository : JpaRepository<SocialLink, Long> {
    fun findByUser(user: User): List<SocialLink>
    fun deleteByUser(user: User)
}
