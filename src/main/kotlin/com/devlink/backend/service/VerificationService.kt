package com.devlink.backend.service

import com.devlink.backend.dto.*
import com.devlink.backend.enums.EntityType

interface VerificationService {
    fun getTeacherQueue(teacherId: Long, entityType: EntityType?): List<VerificationRequestResponse>
    fun evaluateRequest(teacherId: Long, requestId: Long, request: EvaluateVerificationRequest): VerificationRequestResponse
    fun getTeacherAnalytics(teacherId: Long): TeacherAnalyticsResponse
}
