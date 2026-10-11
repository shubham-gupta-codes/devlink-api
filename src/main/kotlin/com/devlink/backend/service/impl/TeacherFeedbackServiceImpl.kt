package com.devlink.backend.service.impl

import com.devlink.backend.dto.TeacherFeedbackResponse
import com.devlink.backend.entity.TeacherFeedback
import com.devlink.backend.exception.ProfileNotFoundException
import com.devlink.backend.repository.StudentProfileRepository
import com.devlink.backend.repository.TeacherFeedbackRepository
import com.devlink.backend.repository.TeacherProfileRepository
import com.devlink.backend.repository.UserRepository
import com.devlink.backend.service.TeacherFeedbackService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class TeacherFeedbackServiceImpl(
    private val teacherFeedbackRepository: TeacherFeedbackRepository,
    private val userRepository: UserRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val studentProfileRepository: StudentProfileRepository
) : TeacherFeedbackService {

    override fun addFeedback(teacherId: Long, studentId: Long, content: String): TeacherFeedbackResponse {
        val teacherUser = userRepository.findById(teacherId)
            .orElseThrow { ProfileNotFoundException(teacherId) }

        val teacherProfile = teacherProfileRepository.findByUser(teacherUser)
            .orElseThrow { ProfileNotFoundException(teacherId) }

        val studentUser = userRepository.findById(studentId)
            .orElseThrow { ProfileNotFoundException(studentId) }

        val studentProfile = studentProfileRepository.findByUser(studentUser)
            .orElseThrow { ProfileNotFoundException(studentId) }

        val existingOpt = teacherFeedbackRepository.findByTeacherAndStudent(teacherProfile, studentProfile)
        val feedback = if (existingOpt.isPresent) {
            val existing = existingOpt.get()
            existing.content = content
            existing.updatedAt = Instant.now()
            existing
        } else {
            TeacherFeedback(
                teacher = teacherProfile,
                student = studentProfile,
                content = content
            )
        }

        val saved = teacherFeedbackRepository.save(feedback)
        return mapToResponse(saved)
    }

    @Transactional(readOnly = true)
    override fun getFeedbacksForStudent(studentId: Long): List<TeacherFeedbackResponse> {
        val studentUser = userRepository.findById(studentId).orElse(null) ?: return emptyList()
        val studentProfile = studentProfileRepository.findByUser(studentUser).orElse(null) ?: return emptyList()
        val feedbacks = teacherFeedbackRepository.findByStudentOrderByCreatedAtDesc(studentProfile)
        return feedbacks.map { mapToResponse(it) }
    }

    private fun mapToResponse(feedback: TeacherFeedback): TeacherFeedbackResponse {
        val teacherUser = feedback.teacher.user
        val name = feedback.teacher.department?.let { "Prof. ${teacherUser.username}" } ?: teacherUser.username
        val dept = feedback.teacher.department ?: "Academic Department"

        return TeacherFeedbackResponse(
            id = feedback.id!!,
            teacherUsername = teacherUser.username,
            teacherName = name,
            department = dept,
            content = feedback.content,
            createdAt = feedback.createdAt
        )
    }
}
