package com.devlink.backend.controller

import com.devlink.backend.dto.CreateFeedbackRequest
import com.devlink.backend.dto.TeacherFeedbackResponse
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.TeacherFeedbackService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/feedback")
class TeacherFeedbackController(
    private val teacherFeedbackService: TeacherFeedbackService
) {

    @PostMapping("/student/{studentId}")
    fun addFeedback(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable studentId: Long,
        @Valid @RequestBody request: CreateFeedbackRequest
    ): ResponseEntity<TeacherFeedbackResponse> {
        val response = teacherFeedbackService.addFeedback(principal.id, studentId, request.content)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/student/{studentId}")
    fun getStudentFeedbacks(
        @PathVariable studentId: Long
    ): ResponseEntity<List<TeacherFeedbackResponse>> {
        val response = teacherFeedbackService.getFeedbacksForStudent(studentId)
        return ResponseEntity.ok(response)
    }
}
