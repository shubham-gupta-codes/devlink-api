package com.devlink.backend.repository

import com.devlink.backend.entity.Notification
import com.devlink.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserOrderByCreatedAtDesc(user: User): List<Notification>
}
