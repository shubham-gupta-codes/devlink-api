package com.devlink.backend.repository

import com.devlink.backend.entity.Project
import com.devlink.backend.enums.VerificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByStudentId(studentId: Long): List<Project>
    
    fun findByStudentIdAndVerificationStatus(studentId: Long, status: VerificationStatus): List<Project>
    
    fun findByVerificationStatus(status: VerificationStatus): List<Project>

    @Query("SELECT p FROM Project p WHERE p.verificationStatus = :status ORDER BY (p.likeCount + p.commentCount) DESC, p.createdAt DESC")
    fun findTrendingProjects(status: VerificationStatus): List<Project>
}
