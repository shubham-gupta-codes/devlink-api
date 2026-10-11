package com.devlink.backend.service.impl

import com.devlink.backend.dto.*
import com.devlink.backend.entity.*
import com.devlink.backend.enums.*
import com.devlink.backend.repository.*
import com.devlink.backend.service.ProjectService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ProjectServiceImpl(
    private val studentProfileRepository: StudentProfileRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val projectImageRepository: ProjectImageRepository,
    private val projectLikeRepository: ProjectLikeRepository,
    private val projectCommentRepository: ProjectCommentRepository,
    private val verificationRequestRepository: VerificationRequestRepository,
    private val notificationRepository: NotificationRepository
) : ProjectService {

    @Transactional
    override fun uploadProject(studentId: Long, request: UploadProjectRequest): ProjectResponse {
        val student = studentProfileRepository.findById(studentId)
            .orElseThrow { IllegalArgumentException("Student profile not found") }

        // 1. Create project with status PENDING
        val project = Project(
            student = student,
            name = request.name,
            description = request.description,
            techStack = request.techStack,
            githubUrl = request.githubUrl,
            demoUrl = request.demoUrl,
            verificationStatus = VerificationStatus.PENDING
        )
        val savedProject = projectRepository.save(project)

        // Save project images if any
        request.imageUrls?.forEach { url ->
            val img = ProjectImage(project = savedProject, imageUrl = url)
            projectImageRepository.save(img)
        }

        // 2. Select teacher using round-robin sequence
        val studentBranch = student.branch ?: throw IllegalArgumentException("Student does not have a branch configured.")
        val teachers = teacherProfileRepository.findAll().filter {
            val tDept = it.department.uppercase()
            val sBranch = studentBranch.name.uppercase()
            tDept.contains(sBranch) ||
            (sBranch == "CS" && tDept.contains("COMPUTER SCIENCE")) ||
            (sBranch == "IT" && tDept.contains("INFORMATION TECHNOLOGY"))
        }
        if (teachers.isEmpty()) {
            throw IllegalArgumentException("No teachers registered in department matching ${studentBranch.name}")
        }
        val assignedTeacher = teachers.minByOrNull { teacher ->
            verificationRequestRepository.countByReviewedById(teacher.id!!)
        } ?: teachers.first()

        // 3. Create Verification Request
        val verificationRequest = VerificationRequest(
            entityType = EntityType.PROJECT,
            entityId = savedProject.id!!,
            requestedBy = student.user,
            status = VerificationStatus.PENDING,
            reviewedBy = userRepository.findById(assignedTeacher.id!!).get()
        )
        verificationRequestRepository.save(verificationRequest)

        // 4. Send notification to the teacher
        val notification = Notification(
            user = userRepository.findById(assignedTeacher.id!!).get(),
            title = "New Project Verification Request",
            message = "@${student.user.username} requested verification for project: ${savedProject.name}"
        )
        notificationRepository.save(notification)

        return mapToResponse(savedProject, studentId)
    }

    override fun getProjectFeed(userId: Long, filter: String): List<ProjectResponse> {
        val projects = when (filter.lowercase()) {
            "trending" -> {
                val allApproved = projectRepository.findByVerificationStatus(VerificationStatus.APPROVED)
                val now = java.time.Instant.now()
                allApproved.sortedByDescending { project ->
                    val likes = project.likeCount
                    val comments = project.commentCount
                    val hoursSince = java.time.Duration.between(project.createdAt, now).toHours()
                    (likes * 1.0 + comments * 2.0) / Math.pow((hoursSince + 2.0), 1.5)
                }
            }
            else -> projectRepository.findByVerificationStatus(VerificationStatus.APPROVED).sortedByDescending { it.createdAt }
        }
        return projects.map { mapToResponse(it, userId) }
    }

    @Transactional
    override fun toggleLike(userId: Long, projectId: Long): ProjectResponse {
        val project = projectRepository.findById(projectId)
            .orElseThrow { IllegalArgumentException("Project not found") }
        val exists = projectLikeRepository.existsByProjectIdAndUserId(projectId, userId)

        if (exists) {
            val like = projectLikeRepository.findByProjectIdAndUserId(projectId, userId)
            if (like != null) {
                projectLikeRepository.delete(like)
                project.likeCount = (project.likeCount - 1).coerceAtLeast(0)
            }
        } else {
            val like = ProjectLike(projectId = projectId, userId = userId)
            projectLikeRepository.save(like)
            project.likeCount = project.likeCount + 1

            // Notify author if liker is not author
            if (project.student.id != userId) {
                val liker = userRepository.findById(userId).get()
                val notification = Notification(
                    user = project.student.user,
                    title = "New Like",
                    message = "@${liker.username} liked your project: ${project.name}"
                )
                notificationRepository.save(notification)
            }
        }
        val updated = projectRepository.save(project)
        return mapToResponse(updated, userId)
    }

    @Transactional
    override fun addComment(userId: Long, projectId: Long, request: ProjectCommentRequest): ProjectCommentResponse {
        val project = projectRepository.findById(projectId)
            .orElseThrow { IllegalArgumentException("Project not found") }
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val parentComment = request.parentCommentId?.let { pid ->
            projectCommentRepository.findById(pid)
                .orElseThrow { IllegalArgumentException("Parent comment not found") }
        }

        val comment = ProjectComment(
            project = project,
            user = user,
            parentComment = parentComment,
            content = request.content
        )
        val savedComment = projectCommentRepository.save(comment)

        // Increment comment count
        project.commentCount = project.commentCount + 1
        projectRepository.save(project)

        // Notify author if commenter is not author
        if (project.student.id != userId) {
            val notification = Notification(
                user = project.student.user,
                title = "New Comment",
                message = "@${user.username} commented on your project: ${project.name}"
            )
            notificationRepository.save(notification)
        }

        return mapCommentToResponse(savedComment)
    }

    override fun getComments(projectId: Long): List<ProjectCommentResponse> {
        val rootComments = projectCommentRepository.findByProjectIdAndParentCommentIsNullOrderByCreatedAtAsc(projectId)
        return rootComments.map { mapCommentToResponse(it) }
    }

    override fun getStudentProjects(studentId: Long, currentUserId: Long): List<ProjectResponse> {
        // If current user is the owner, return all projects, otherwise return only APPROVED ones
        val projects = if (studentId == currentUserId) {
            projectRepository.findByStudentId(studentId)
        } else {
            projectRepository.findByStudentIdAndVerificationStatus(studentId, VerificationStatus.APPROVED)
        }
        return projects.map { mapToResponse(it, currentUserId) }
    }

    @Transactional
    override fun deleteProject(studentId: Long, projectId: Long) {
        val project = projectRepository.findById(projectId)
            .orElseThrow { IllegalArgumentException("Project not found") }
        if (project.student.id != studentId) {
            throw SecurityException("You do not own this project")
        }
        projectRepository.delete(project)
    }

    private fun mapToResponse(project: Project, userId: Long): ProjectResponse {
        val imageUrls = projectImageRepository.findAll().filter { it.project.id == project.id }
            .map { it.imageUrl }
        val isLiked = projectLikeRepository.existsByProjectIdAndUserId(project.id!!, userId)

        return ProjectResponse(
            id = project.id!!,
            studentUserId = project.student.id!!,
            studentUsername = project.student.user.username,
            studentName = project.student.name,
            studentPhotoUrl = project.student.photoUrl,
            name = project.name,
            description = project.description,
            techStack = project.techStack,
            githubUrl = project.githubUrl,
            demoUrl = project.demoUrl,
            verificationStatus = project.verificationStatus,
            likeCount = project.likeCount,
            commentCount = project.commentCount,
            createdAt = project.createdAt,
            imageUrls = imageUrls,
            likedByCurrentUser = isLiked
        )
    }

    private fun mapCommentToResponse(comment: ProjectComment): ProjectCommentResponse {
        val studentProfile = studentProfileRepository.findById(comment.user.id!!)
        val studentName = studentProfile
            .map { it.name }
            .orElse(comment.user.username)
        val photoUrl = studentProfile
            .map { it.photoUrl }
            .orElse(null)

        return ProjectCommentResponse(
            id = comment.id!!,
            projectId = comment.project.id!!,
            userId = comment.user.id!!,
            username = comment.user.username,
            name = studentName,
            photoUrl = photoUrl,
            content = comment.content,
            createdAt = comment.createdAt,
            replies = comment.replies.map { mapCommentToResponse(it) }
        )
    }
}
