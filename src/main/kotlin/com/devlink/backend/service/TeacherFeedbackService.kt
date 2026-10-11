package com.devlink.backend.service

import com.devlink.backend.dto.TeacherFeedbackResponse

interface TeacherFeedbackService {
    fun addFeedback(teacherId: Long, studentId: Long, content: String): TeacherFeedbackResponse
    fun getFeedbacksForStudent(studentId: Long): List<TeacherFeedbackResponse>
}
