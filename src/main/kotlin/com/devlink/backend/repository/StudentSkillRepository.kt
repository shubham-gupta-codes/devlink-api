package com.devlink.backend.repository

import com.devlink.backend.entity.StudentProfile
import com.devlink.backend.entity.StudentSkill
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StudentSkillRepository : JpaRepository<StudentSkill, Long> {
    fun findByStudent(student: StudentProfile): List<StudentSkill>
}
