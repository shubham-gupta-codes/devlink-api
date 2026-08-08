package com.devlink.backend.controller

import com.devlink.backend.dto.*
import com.devlink.backend.enums.EntityType
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.VerificationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/verifications")
class VerificationController(
    private val verificationService: VerificationService
) {

    @GetMapping("/queue")
    fun getTeacherQueue(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(required = false) entityType: EntityType?
    ): ResponseEntity<List<VerificationRequestResponse>> {
        val queue = verificationService.getTeacherQueue(principal.id, entityType)
        return ResponseEntity.ok(queue)
    }

    @PostMapping("/{requestId}/evaluate")
    fun evaluateRequest(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable requestId: Long,
        @Valid @RequestBody request: EvaluateVerificationRequest
    ): ResponseEntity<VerificationRequestResponse> {
        val response = verificationService.evaluateRequest(principal.id, requestId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/analytics")
    fun getTeacherAnalytics(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<TeacherAnalyticsResponse> {
        val analytics = verificationService.getTeacherAnalytics(principal.id)
        return ResponseEntity.ok(analytics)
    }
}
