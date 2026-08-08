package com.devlink.backend.service.impl

import com.devlink.backend.dto.NotificationResponse
import com.devlink.backend.exception.InvalidCredentialsException
import com.devlink.backend.repository.NotificationRepository
import com.devlink.backend.repository.UserRepository
import com.devlink.backend.service.NotificationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : NotificationService {

    override fun getUserNotifications(userId: Long): List<NotificationResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { InvalidCredentialsException() }
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).map {
            NotificationResponse(
                id = it.id!!,
                title = it.title,
                message = it.message,
                read = it.read,
                createdAt = it.createdAt
            )
        }
    }

    @Transactional
    override fun markAsRead(userId: Long, notificationId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException("Notification not found") }
        if (notification.user.id == userId) {
            notification.read = true
            notificationRepository.save(notification)
        }
    }
}
