package com.devlink.backend.controller

import com.devlink.backend.dto.*
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.SkillService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/skills")
class SkillController(
    private val skillService: SkillService
) {

    @GetMapping
    fun getAllSkills(): ResponseEntity<List<SkillResponse>> =
        ResponseEntity.ok(skillService.getAllSkills())

    @PostMapping("/request")
    fun requestSkillVerification(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: SkillRequestDto
    ): ResponseEntity<SkillRequestResponse> =
        ResponseEntity.ok(skillService.requestSkillVerification(principal.id, request.skillId))

    @GetMapping("/request/student")
    fun getStudentRequests(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<SkillRequestResponse>> =
        ResponseEntity.ok(skillService.getStudentRequests(principal.id))

    @GetMapping("/student")
    fun getStudentApprovedSkills(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<StudentSkillResponse>> =
        ResponseEntity.ok(skillService.getStudentApprovedSkills(principal.id))

    @GetMapping("/student/{studentId}")
    fun getStudentApprovedSkillsById(
        @PathVariable studentId: Long
    ): ResponseEntity<List<StudentSkillResponse>> =
        ResponseEntity.ok(skillService.getStudentApprovedSkills(studentId))

    @GetMapping("/request/teacher")
    fun getTeacherPendingRequests(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<SkillRequestResponse>> =
        ResponseEntity.ok(skillService.getTeacherPendingRequests(principal.id))

    @PostMapping("/request/{requestId}/meet")
    fun scheduleMeeting(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable requestId: Long,
        @RequestBody request: ScheduleMeetingRequest
    ): ResponseEntity<SkillRequestResponse> =
        ResponseEntity.ok(skillService.scheduleMeeting(principal.id, requestId, request.meetingDate, request.notes))

    @PostMapping("/request/{requestId}/approve")
    fun approveRequest(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable requestId: Long,
        @RequestBody request: ApproveSkillRequest
    ): ResponseEntity<SkillRequestResponse> =
        ResponseEntity.ok(skillService.approveRequest(principal.id, requestId, request.level, request.points, request.remarks))

    @PostMapping("/request/{requestId}/reject")
    fun rejectRequest(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable requestId: Long,
        @RequestBody request: RejectSkillRequest
    ): ResponseEntity<SkillRequestResponse> =
        ResponseEntity.ok(skillService.rejectRequest(principal.id, requestId, request.reason))
}
