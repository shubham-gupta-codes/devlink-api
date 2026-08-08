package com.devlink.backend.repository

import com.devlink.backend.entity.TeacherFeedback
import com.devlink.backend.entity.StudentProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeacherFeedbackRepository : JpaRepository<TeacherFeedback, Long> {
    fun findByStudentOrderByCreatedAtDesc(student: StudentProfile): List<TeacherFeedback>
}
