package com.devlink.backend.controller

import com.devlink.backend.dto.*
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.JobService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/jobs")
class JobController(
    private val jobService: JobService
) {
    @PostMapping
    fun createJob(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateJobRequest
    ): ResponseEntity<JobResponse> {
        return ResponseEntity.ok(jobService.createJob(principal.id, request))
    }

    @PutMapping("/{id}")
    fun updateJob(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: UpdateJobRequest
    ): ResponseEntity<JobResponse> {
        return ResponseEntity.ok(jobService.updateJob(principal.id, id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteJob(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Map<String, String>> {
        jobService.deleteJob(principal.id, id)
        return ResponseEntity.ok(mapOf("message" to "Job deleted successfully"))
    }

    @GetMapping("/recruiter")
    fun getRecruiterJobs(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<JobResponse>> {
        return ResponseEntity.ok(jobService.getRecruiterJobs(principal.id))
    }

    @GetMapping("/open")
    fun getAllOpenJobs(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<JobResponse>> {
        return ResponseEntity.ok(jobService.getAllOpenJobs(principal.id))
    }

    @GetMapping("/{id}")
    fun getJobDetails(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<JobResponse> {
        return ResponseEntity.ok(jobService.getJobDetails(id, principal.id))
    }

    @PostMapping("/{id}/apply")
    fun applyToJob(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<JobApplicationResponse> {
        return ResponseEntity.ok(jobService.applyToJob(principal.id, id))
    }

    @GetMapping("/applications/my")
    fun getStudentApplications(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<JobApplicationResponse>> {
        return ResponseEntity.ok(jobService.getStudentApplications(principal.id))
    }

    @GetMapping("/{id}/applications")
    fun getJobApplications(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<JobApplicationResponse>> {
        return ResponseEntity.ok(jobService.getJobApplications(id, principal.id))
    }

    @PutMapping("/applications/{appId}")
    fun evaluateApplication(
        @PathVariable appId: Long,
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: EvaluateApplicationRequest
    ): ResponseEntity<JobApplicationResponse> {
        return ResponseEntity.ok(jobService.evaluateApplication(principal.id, appId, request.status))
    }

    @PostMapping("/shortlist")
    fun shortlistCandidate(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam studentId: Long,
        @RequestParam(required = false) jobId: Long?
    ): ResponseEntity<SavedCandidateResponse> {
        return ResponseEntity.ok(jobService.shortlistCandidate(principal.id, studentId, jobId))
    }

    @GetMapping("/shortlisted")
    fun getSavedCandidates(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<SavedCandidateResponse>> {
        return ResponseEntity.ok(jobService.getSavedCandidates(principal.id))
    }

    @DeleteMapping("/shortlist")
    fun removeShortlistedCandidate(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam studentId: Long,
        @RequestParam(required = false) jobId: Long?
    ): ResponseEntity<Void> {
        jobService.removeSavedCandidate(principal.id, studentId, jobId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/shortlist/{id}")
    fun removeShortlistedCandidateById(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        jobService.removeSavedCandidateById(principal.id, id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/shortlist/student/{studentId}")
    fun removeCandidateFromAllShortlists(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable studentId: Long
    ): ResponseEntity<Void> {
        jobService.removeCandidateFromAllShortlists(principal.id, studentId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/recommended")
    fun getRecommendedCandidates(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<ProfileResponse>> {
        return ResponseEntity.ok(jobService.getRecommendedCandidates(principal.id, id))
    }
}
