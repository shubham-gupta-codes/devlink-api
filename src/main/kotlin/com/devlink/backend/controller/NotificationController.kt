package com.devlink.backend.controller

import com.devlink.backend.dto.NotificationResponse
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    fun getMyNotifications(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<NotificationResponse>> {
        return ResponseEntity.ok(notificationService.getUserNotifications(principal.id))
    }

    @PostMapping("/{id}/read")
    fun markAsRead(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        notificationService.markAsRead(principal.id, id)
        return ResponseEntity.ok().build()
    }
}
