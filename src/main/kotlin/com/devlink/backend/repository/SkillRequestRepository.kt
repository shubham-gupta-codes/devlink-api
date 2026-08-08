package com.devlink.backend.repository

import com.devlink.backend.entity.SkillRequest
import com.devlink.backend.entity.StudentProfile
import com.devlink.backend.entity.TeacherProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SkillRequestRepository : JpaRepository<SkillRequest, Long> {
    fun findByStudent(student: StudentProfile): List<SkillRequest>
    fun findByTeacher(teacher: TeacherProfile): List<SkillRequest>
    fun countByTeacher(teacher: TeacherProfile): Long
}
