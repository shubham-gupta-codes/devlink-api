package com.devlink.backend.service

import com.devlink.backend.dto.*

interface ProjectService {
    fun uploadProject(studentId: Long, request: UploadProjectRequest): ProjectResponse
    fun getProjectFeed(userId: Long, filter: String): List<ProjectResponse>
    fun toggleLike(userId: Long, projectId: Long): ProjectResponse
    fun addComment(userId: Long, projectId: Long, request: ProjectCommentRequest): ProjectCommentResponse
    fun getComments(projectId: Long): List<ProjectCommentResponse>
    fun getStudentProjects(studentId: Long, currentUserId: Long): List<ProjectResponse>
    fun deleteProject(studentId: Long, projectId: Long)
}
