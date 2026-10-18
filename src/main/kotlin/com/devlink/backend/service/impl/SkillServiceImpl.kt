package com.devlink.backend.service.impl

import com.devlink.backend.dto.*
import com.devlink.backend.entity.*
import com.devlink.backend.enums.SkillLevel
import com.devlink.backend.enums.SkillStatus
import com.devlink.backend.enums.PointSource
import com.devlink.backend.exception.ProfileNotFoundException
import com.devlink.backend.repository.*
import com.devlink.backend.service.SkillService
import com.devlink.backend.service.BadgeEvaluator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SkillServiceImpl(
    private val skillRepository: SkillRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val skillRequestRepository: SkillRequestRepository,
    private val studentSkillRepository: StudentSkillRepository,
    private val notificationRepository: NotificationRepository,
    private val pointsLedgerRepository: PointsLedgerRepository,
    private val teacherFeedbackRepository: TeacherFeedbackRepository,
    private val badgeEvaluator: BadgeEvaluator,
    private val conversationRepository: ConversationRepository,
    private val conversationParticipantRepository: ConversationParticipantRepository,
    private val messageRepository: MessageRepository
) : SkillService {

    override fun getAllSkills(): List<SkillResponse> {
        return skillRepository.findAll().map { SkillResponse(it.id!!, it.name) }
    }

    @Transactional
    override fun requestSkillVerification(studentId: Long, skillId: Long): SkillRequestResponse {
        val student = studentProfileRepository.findById(studentId)
            .orElseThrow { ProfileNotFoundException(studentId) }
        val skill = skillRepository.findById(skillId)
            .orElseThrow { IllegalArgumentException("Skill not found") }

        // Find all teachers in the student's branch/department
        val studentBranch = student.branch ?: throw IllegalArgumentException("Student does not have a department/branch configured.")
        val teachers = teacherProfileRepository.findAll().filter {
            val tDept = it.department.uppercase()
            val sBranch = studentBranch.name.uppercase()
            tDept.contains(sBranch) || 
            (sBranch == "CS" && tDept.contains("COMPUTER SCIENCE")) ||
            (sBranch == "IT" && tDept.contains("INFORMATION TECHNOLOGY"))
        }

        if (teachers.isEmpty()) {
            throw IllegalArgumentException("No teachers are currently registered in your department (${studentBranch.name}) to evaluate skills.")
        }

        // Automatic round-robin assignment: pick teacher with fewest total all-time requests
        val assignedTeacher = teachers.minByOrNull { teacher ->
            skillRequestRepository.countByTeacher(teacher)
        } ?: throw IllegalStateException("Could not find an available teacher for assignment")

        val request = SkillRequest(
            student = student,
            skill = skill,
            teacher = assignedTeacher,
            status = SkillStatus.PENDING
        )
        val savedRequest = skillRequestRepository.save(request)

        // Send a notification to the teacher
        val teacherNotification = Notification(
            user = assignedTeacher.user,
            title = "New Evaluation Request",
            message = "Student ${student.name} (@${student.user.username}) requested evaluation for \"${skill.name}\"."
        )
        notificationRepository.save(teacherNotification)

        return savedRequest.toResponse()
    }

    override fun getStudentRequests(studentId: Long): List<SkillRequestResponse> {
        val student = studentProfileRepository.findById(studentId)
            .orElseThrow { ProfileNotFoundException(studentId) }
        return skillRequestRepository.findByStudent(student).map { it.toResponse() }
    }

    override fun getStudentApprovedSkills(studentId: Long): List<StudentSkillResponse> {
        val student = studentProfileRepository.findById(studentId)
            .orElseThrow { ProfileNotFoundException(studentId) }
        return studentSkillRepository.findByStudent(student).map {
            StudentSkillResponse(
                id = it.id!!,
                skillName = it.skill.name,
                level = it.level,
                pointsAwarded = it.pointsAwarded,
                approvedByTeacherName = it.approvedBy?.user?.username,
                approvedAt = it.approvedAt
            )
        }
    }

    override fun getTeacherPendingRequests(teacherId: Long): List<SkillRequestResponse> {
        val teacher = teacherProfileRepository.findById(teacherId)
            .orElseThrow { ProfileNotFoundException(teacherId) }
        return skillRequestRepository.findByTeacher(teacher).map { it.toResponse() }
    }

    @Transactional
    override fun scheduleMeeting(
        teacherId: Long,
        requestId: Long,
        meetingDate: Instant,
        notes: String
    ): SkillRequestResponse {
        val request = skillRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Request not found") }
        if (request.teacher.id != teacherId) {
            throw IllegalArgumentException("You are not authorized to evaluate this request")
        }

        request.status = SkillStatus.MEET
        request.meetingDate = meetingDate
        request.meetingNotes = notes
        request.updatedAt = Instant.now()
        val saved = skillRequestRepository.save(request)

        val studentNotification = Notification(
            user = request.student.user,
            title = "Evaluation Scheduled",
            message = "Your skill evaluation for \"${request.skill.name}\" has been scheduled for $meetingDate. Notes: $notes"
        )
        notificationRepository.save(studentNotification)

        sendEvaluationChatRemark(
            request.teacher.user,
            request.student.user,
            "Scheduled skill evaluation for ${request.skill.name} on $meetingDate. Notes: $notes"
        )

        return saved.toResponse()
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

    @Transactional
    override fun approveRequest(
        teacherId: Long,
        requestId: Long,
        level: SkillLevel,
        points: Int,
        remarks: String?
    ): SkillRequestResponse {
        val request = skillRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Request not found") }
        if (request.teacher.id != teacherId) {
            throw IllegalArgumentException("You are not authorized to evaluate this request")
        }

        request.status = SkillStatus.ACCEPTED
        request.updatedAt = Instant.now()
        val saved = skillRequestRepository.save(request)

        // Save student skill
        val studentSkill = StudentSkill(
            student = request.student,
            skill = request.skill,
            level = level,
            pointsAwarded = points,
            approvedBy = request.teacher
        )
        val savedSkill = studentSkillRepository.save(studentSkill)

        // Add points to student profile
        request.student.totalPoints = (request.student.totalPoints ?: 0) + points
        studentProfileRepository.save(request.student)

        // Add to points ledger
        val ledger = PointsLedger(
            student = request.student,
            pointSource = PointSource.VERIFIED_SKILL,
            points = points,
            referenceId = savedSkill.id!!,
            teacher = request.teacher.user
        )
        pointsLedgerRepository.save(ledger)

        // Save teacher feedback
        if (!remarks.isNullOrBlank()) {
            teacherFeedbackRepository.save(
                TeacherFeedback(
                    student = request.student,
                    teacher = request.teacher,
                    content = remarks
                )
            )
            sendEvaluationChatRemark(
                request.teacher.user,
                request.student.user,
                "Approved skill request for ${request.skill.name}: $remarks"
            )
        } else {
            sendEvaluationChatRemark(
                request.teacher.user,
                request.student.user,
                "Approved skill request for ${request.skill.name} with +$points points."
            )
        }

        // Evaluate badges
        badgeEvaluator.evaluateBadges(request.student)

        // Notify student
        val studentNotification = Notification(
            user = request.student.user,
            title = "Skill Approved!",
            message = "Your skill request for \"${request.skill.name}\" was approved at $level level by @${request.teacher.user.username}. +$points points awarded!"
        )
        notificationRepository.save(studentNotification)

        return saved.toResponse()
    }

    @Transactional
    override fun rejectRequest(
        teacherId: Long,
        requestId: Long,
        reason: String
    ): SkillRequestResponse {
        val request = skillRequestRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Request not found") }
        if (request.teacher.id != teacherId) {
            throw IllegalArgumentException("You are not authorized to evaluate this request")
        }

        request.status = SkillStatus.REJECTED
        request.rejectionReason = reason
        request.updatedAt = Instant.now()
        val saved = skillRequestRepository.save(request)

        // Notify student
        val studentNotification = Notification(
            user = request.student.user,
            title = "Skill Evaluation Rejected",
            message = "Your skill request for \"${request.skill.name}\" was rejected. Reason: $reason"
        )
        notificationRepository.save(studentNotification)

        sendEvaluationChatRemark(
            request.teacher.user,
            request.student.user,
            "Rejected skill request for ${request.skill.name}. Reason: $reason"
        )

        return saved.toResponse()
    }

    private fun SkillRequest.toResponse(): SkillRequestResponse {
        return SkillRequestResponse(
            id = this.id!!,
            studentName = this.student.name,
            studentUsername = this.student.user.username,
            skillName = this.skill.name,
            teacherName = this.teacher.user.username,
            status = this.status,
            meetingDate = this.meetingDate,
            meetingNotes = this.meetingNotes,
            rejectionReason = this.rejectionReason,
            createdAt = this.createdAt,
            studentPhotoUrl = this.student.photoUrl
        )
    }
}
