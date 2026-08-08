package com.devlink.backend.service

import com.devlink.backend.dto.NotificationResponse

interface NotificationService {
    fun getUserNotifications(userId: Long): List<NotificationResponse>
    fun markAsRead(userId: Long, notificationId: Long)
}
