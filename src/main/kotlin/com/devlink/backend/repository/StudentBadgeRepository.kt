package com.devlink.backend.repository

import com.devlink.backend.entity.StudentBadge
import com.devlink.backend.entity.StudentProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StudentBadgeRepository : JpaRepository<StudentBadge, Long> {
    fun findByStudent(student: StudentProfile): List<StudentBadge>
    fun existsByStudentAndBadgeName(student: StudentProfile, badgeName: String): Boolean
}
