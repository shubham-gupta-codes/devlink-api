package com.devlink.backend.service.impl

import com.devlink.backend.dto.*
import com.devlink.backend.entity.*
import com.devlink.backend.enums.*
import com.devlink.backend.repository.*
import com.devlink.backend.service.VerificationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class VerificationServiceImpl(
    private val verificationRequestRepository: VerificationRequestRepository,
    private val projectRepository: ProjectRepository,
    private val certificateRepository: CertificateRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val pointsLedgerRepository: PointsLedgerRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val projectImageRepository: ProjectImageRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val teacherFeedbackRepository: TeacherFeedbackRepository,
    private val badgeEvaluator: com.devlink.backend.service.BadgeEvaluator,
    private val conversationRepository: ConversationRepository,
    private val conversationParticipantRepository: ConversationParticipantRepository,
    private val messageRepository: MessageRepository
) : VerificationService {

    override fun getTeacherQueue(teacherId: Long, entityType: EntityType?): List<VerificationRequestResponse> {
        val requests = if (entityType != null) {
            verificationRequestRepository.findByReviewedByIdAndEntityTypeAndStatus(teacherId, entityType, VerificationStatus.PENDING)
        } else {
            verificationRequestRepository.findByReviewedByIdAndStatus(teacherId, VerificationStatus.PENDING)
        }
        return requests.map { mapToResponse(it) }
    }

    @Transactional
    override fun evaluateRequest(teacherId: Long, requestId: Long, request: EvaluateVerificationRequest): VerificationRequestResponse {
        val verificationRequest = verificationRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Verification request not found") }

        if (verificationRequest.reviewedBy.id != teacherId) {
            throw SecurityException("You are not assigned to review this request")
        }

        val teacherProfile = teacherProfileRepository.findById(teacherId)
            .orElseThrow { IllegalArgumentException("Teacher profile not found") }

        verificationRequest.status = request.status
        verificationRequest.remarks = request.remarks
        verificationRequest.reviewedAt = Instant.now()
        verificationRequest.scheduleAt = request.scheduleAt

        val student = studentProfileRepository.findById(verificationRequest.requestedBy.id!!)
            .orElseThrow { IllegalArgumentException("Student profile not found") }

        when (verificationRequest.entityType) {
            EntityType.PROJECT -> {
                val project = projectRepository.findById(verificationRequest.entityId)
                    .orElseThrow { IllegalArgumentException("Project not found") }
                project.verificationStatus = request.status
                projectRepository.save(project)

                if (request.status == VerificationStatus.APPROVED) {
                    val pts = request.points ?: 10
                    // Add to points ledger
                    val ledger = PointsLedger(
                        student = student,
                        pointSource = PointSource.VERIFIED_PROJECT,
                        points = pts,
                        referenceId = project.id!!,
                        teacher = verificationRequest.reviewedBy
                    )
                    pointsLedgerRepository.save(ledger)

                    // Update student points
                    student.totalPoints = (student.totalPoints ?: 0) + pts
                    studentProfileRepository.save(student)

                    // Save teacher feedback
                    if (!request.remarks.isNullOrBlank()) {
                        teacherFeedbackRepository.save(
                            TeacherFeedback(
                                student = student,
                                teacher = teacherProfile,
                                content = request.remarks
                            )
                        )
                    }

                    // Evaluate badges
                    badgeEvaluator.evaluateBadges(student)

                    // Notify student
                    val notification = Notification(
                        user = student.user,
                        title = "Project Approved!",
                        message = "Your project '${project.name}' has been verified by @${teacherProfile.user.username}. Points earned: $pts."
                    )
                    notificationRepository.save(notification)

                    sendEvaluationChatRemark(
                        verificationRequest.reviewedBy,
                        student.user,
                        "Approved project '${project.name}' with +$pts points. Remarks: ${request.remarks ?: ""}"
                    )
                } else if (request.status == VerificationStatus.REJECTED) {
                    val notification = Notification(
                        user = student.user,
                        title = "Project Rejected",
                        message = "Your project '${project.name}' is rejected. Reason: ${request.remarks ?: "Does not meet requirements."}"
                    )
                    notificationRepository.save(notification)

                    sendEvaluationChatRemark(
                        verificationRequest.reviewedBy,
                        student.user,
                        "Rejected project '${project.name}'. Reason: ${request.remarks ?: "Does not meet requirements."}"
                    )
                } else if (request.status == VerificationStatus.NEED_TO_TALK) {
                    val notification = Notification(
                        user = student.user,
                        title = "Project Evaluation Scheduled",
                        message = "Teacher wants to talk regarding project '${project.name}'. Remarks: ${request.remarks ?: ""}"
                    )
                    notificationRepository.save(notification)

                    sendEvaluationChatRemark(
                        verificationRequest.reviewedBy,
                        student.user,
                        "Scheduled discussion for project '${project.name}'. Remarks: ${request.remarks ?: ""}"
                    )
                }
            }
            EntityType.CERTIFICATE -> {
                val certificate = certificateRepository.findById(verificationRequest.entityId)
                    .orElseThrow { IllegalArgumentException("Certificate not found") }
                certificate.verificationStatus = request.status
                certificateRepository.save(certificate)

                if (request.status == VerificationStatus.APPROVED) {
                    val pts = request.points ?: 10
                    // Add to points ledger
                    val ledger = PointsLedger(
                        student = student,
                        pointSource = PointSource.VERIFIED_CERTIFICATE,
                        points = pts,
                        referenceId = certificate.id!!,
                        teacher = verificationRequest.reviewedBy
                    )
                    pointsLedgerRepository.save(ledger)

                    // Update student points
                    student.totalPoints = (student.totalPoints ?: 0) + pts
                    studentProfileRepository.save(student)

                    // Save teacher feedback
                    // Save teacher feedback
                    if (!request.remarks.isNullOrBlank()) {
                        teacherFeedbackRepository.save(
                            TeacherFeedback(
                                student = student,
                                teacher = teacherProfile,
                                content = request.remarks
                            )
                        )
                    }

                    // Evaluate badges
                    badgeEvaluator.evaluateBadges(student)

                    // Notify student
                    val notification = Notification(
                        user = student.user,
                        title = "Certificate Approved!",
                        message = "Your certificate '${certificate.title}' has been verified by @${teacherProfile.user.username}. Points earned: $pts."
                    )
                    notificationRepository.save(notification)

                    sendEvaluationChatRemark(
                        verificationRequest.reviewedBy,
                        student.user,
                        "Approved certificate '${certificate.title}' with +$pts points. Remarks: ${request.remarks ?: ""}"
                    )
                } else if (request.status == VerificationStatus.REJECTED) {
                    val notification = Notification(
                        user = student.user,
                        title = "Certificate Rejected",
                        message = "Your certificate '${certificate.title}' is rejected. Reason: ${request.remarks ?: "Does not meet requirements."}"
                    )
                    notificationRepository.save(notification)

                    sendEvaluationChatRemark(
                        verificationRequest.reviewedBy,
                        student.user,
                        "Rejected certificate '${certificate.title}'. Reason: ${request.remarks ?: "Does not meet requirements."}"
                    )
                } else if (request.status == VerificationStatus.NEED_TO_TALK) {
                    val notification = Notification(
                        user = student.user,
                        title = "Certificate Evaluation Scheduled",
                        message = "Teacher wants to talk regarding certificate '${certificate.title}'. Remarks: ${request.remarks ?: ""}"
                    )
                    notificationRepository.save(notification)

                    sendEvaluationChatRemark(
                        verificationRequest.reviewedBy,
                        student.user,
                        "Scheduled discussion for certificate '${certificate.title}'. Remarks: ${request.remarks ?: ""}"
                    )
                }
            }
            else -> {}
        }

        val saved = verificationRequestRepository.save(verificationRequest)
        return mapToResponse(saved)
    }

    private fun sendEvaluationChatRemark(teacherUser: User, studentUser: User, remark: String) {
        val existing = conversationRepository.findDirectConversationBetween(teacherUser.id!!, studentUser.id!!)
        val conversation = if (existing.isNotEmpty()) {
            existing.first()
        } else {
            val newConv = Conversation(type = "DIRECT")
            val saved = conversationRepository.save(newConv)
            val p1 = ConversationParticipant(conversationId = saved.id!!, userId = teacherUser.id!!)
            val p2 = ConversationParticipant(conversationId = saved.id!!, userId = studentUser.id!!)
            conversationParticipantRepository.saveAll(listOf(p1, p2))
            saved
        }
        val message = Message(
            conversation = conversation,
            sender = teacherUser,
            content = remark
        )
        messageRepository.save(message)
    }

    override fun getTeacherAnalytics(teacherId: Long): TeacherAnalyticsResponse {
        val approvedProjects = projectRepository.findByVerificationStatus(VerificationStatus.APPROVED)
        
        val languages = listOf("Kotlin", "Python", "Java", "JavaScript", "C#", "Spring Boot")
        val langCounts = mutableMapOf<String, Long>()
        val studentUniqueLanguages = mutableMapOf<String, MutableSet<Long>>()

        languages.forEach {
            langCounts[it] = 0L
            studentUniqueLanguages[it] = mutableSetOf()
        }
        langCounts["Others"] = 0L
        studentUniqueLanguages["Others"] = mutableSetOf()

        approvedProjects.forEach { project ->
            val tags = project.techStack.split(",").map { it.trim().lowercase() }
            var categorized = false
            tags.forEach { tag ->
                val matchedLang = languages.find { it.lowercase() == tag }
                if (matchedLang != null) {
                    langCounts[matchedLang] = langCounts[matchedLang]!! + 1
                    studentUniqueLanguages[matchedLang]!!.add(project.student.id!!)
                    categorized = true
                }
            }
            if (!categorized && project.techStack.isNotBlank()) {
                langCounts["Others"] = langCounts["Others"]!! + 1
                studentUniqueLanguages["Others"]!!.add(project.student.id!!)
            }
        }

        val studentCounts = studentUniqueLanguages.map { (lang, studentIds) ->
            StudentCountByLanguage(lang, studentIds.size.toLong())
        }

        return TeacherAnalyticsResponse(
            languageCounts = langCounts,
            studentCountsByLanguage = studentCounts
        )
    }

    private fun mapToResponse(req: VerificationRequest): VerificationRequestResponse {
        val studentName = studentProfileRepository.findById(req.requestedBy.id!!)
            .map { it.name }
            .orElse(req.requestedBy.username)

        val reviewerName = studentProfileRepository.findById(req.reviewedBy.id!!)
            .map { it.name }
            .orElse(req.reviewedBy.username)

        var entityName = ""
        var projectDetails: ProjectResponse? = null
        var certificateDetails: CertificateResponse? = null

        when (req.entityType) {
            EntityType.PROJECT -> {
                val project = projectRepository.findById(req.entityId).orElse(null)
                if (project != null) {
                    entityName = project.name
                    val imageUrls = projectImageRepository.findAll().filter { it.project.id == project.id }
                        .map { it.imageUrl }
                    projectDetails = ProjectResponse(
                        id = project.id!!,
                        studentUserId = project.student.id!!,
                        studentUsername = project.student.user.username,
                        studentName = project.student.name,
                        name = project.name,
                        description = project.description,
                        techStack = project.techStack,
                        githubUrl = project.githubUrl,
                        demoUrl = project.demoUrl,
                        verificationStatus = project.verificationStatus,
                        likeCount = project.likeCount,
                        commentCount = project.commentCount,
                        createdAt = project.createdAt,
                        imageUrls = imageUrls
                    )
                }
            }
            EntityType.CERTIFICATE -> {
                val cert = certificateRepository.findById(req.entityId).orElse(null)
                if (cert != null) {
                    entityName = cert.title
                    certificateDetails = CertificateResponse(
                        id = cert.id!!,
                        studentUserId = cert.student.id!!,
                        studentUsername = cert.student.user.username,
                        studentName = cert.student.name,
                        title = cert.title,
                        issuingOrg = cert.issuingOrg,
                        purpose = cert.purpose,
                        fileUrl = cert.fileUrl,
                        fileType = cert.fileType,
                        verificationStatus = cert.verificationStatus,
                        teacherRemarks = req.remarks
                    )
                }
            }
            EntityType.SKILL -> {
                entityName = "Skill Verification"
            }
        }

        return VerificationRequestResponse(
            id = req.id!!,
            entityType = req.entityType,
            entityId = req.entityId,
            entityName = entityName,
            requestedByUserId = req.requestedBy.id!!,
            requestedByUsername = req.requestedBy.username,
            requestedByName = studentName,
            status = req.status,
            reviewedByUserId = req.reviewedBy.id!!,
            reviewedByUsername = req.reviewedBy.username,
            reviewedByName = reviewerName,
            reviewedAt = req.reviewedAt,
            remarks = req.remarks,
            scheduleAt = req.scheduleAt,
            projectDetails = projectDetails,
            certificateDetails = certificateDetails
        )
    }
}
