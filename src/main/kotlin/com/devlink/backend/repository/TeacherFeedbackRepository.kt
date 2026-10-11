package com.devlink.backend.repository

import com.devlink.backend.entity.StudentProfile
import com.devlink.backend.entity.TeacherFeedback
import com.devlink.backend.entity.TeacherProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TeacherFeedbackRepository : JpaRepository<TeacherFeedback, Long> {
    fun findByStudentOrderByCreatedAtDesc(student: StudentProfile): List<TeacherFeedback>
    fun findByStudentIdOrderByCreatedAtDesc(studentId: Long): List<TeacherFeedback>
    fun findByTeacherAndStudent(teacher: TeacherProfile, student: StudentProfile): Optional<TeacherFeedback>
}
