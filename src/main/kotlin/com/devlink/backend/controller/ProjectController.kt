package com.devlink.backend.controller

import com.devlink.backend.dto.*
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.ProjectService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/projects")
class ProjectController(
    private val projectService: ProjectService
) {

    @PostMapping
    fun uploadProject(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UploadProjectRequest
    ): ResponseEntity<ProjectResponse> {
        val project = projectService.uploadProject(principal.id, request)
        return ResponseEntity.ok(project)
    }

    @GetMapping("/feed")
    fun getProjectFeed(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "all") filter: String
    ): ResponseEntity<List<ProjectResponse>> {
        val feed = projectService.getProjectFeed(principal.id, filter)
        return ResponseEntity.ok(feed)
    }

    @PostMapping("/{projectId}/like")
    fun toggleLike(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable projectId: Long
    ): ResponseEntity<ProjectResponse> {
        val project = projectService.toggleLike(principal.id, projectId)
        return ResponseEntity.ok(project)
    }

    @PostMapping("/{projectId}/comments")
    fun addComment(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable projectId: Long,
        @Valid @RequestBody request: ProjectCommentRequest
    ): ResponseEntity<ProjectCommentResponse> {
        val comment = projectService.addComment(principal.id, projectId, request)
        return ResponseEntity.ok(comment)
    }

    @GetMapping("/{projectId}/comments")
    fun getComments(@PathVariable projectId: Long): ResponseEntity<List<ProjectCommentResponse>> {
        val comments = projectService.getComments(projectId)
        return ResponseEntity.ok(comments)
    }

    @GetMapping("/student/{studentId}")
    fun getStudentProjects(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable studentId: Long
    ): ResponseEntity<List<ProjectResponse>> {
        val projects = projectService.getStudentProjects(studentId, principal.id)
        return ResponseEntity.ok(projects)
    }

    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable projectId: Long
    ): ResponseEntity<Map<String, String>> {
        projectService.deleteProject(principal.id, projectId)
        return ResponseEntity.ok(mapOf("message" to "Project deleted successfully"))
    }
}
