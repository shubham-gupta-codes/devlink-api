package com.devlink.backend.repository

import com.devlink.backend.entity.Certificate
import com.devlink.backend.enums.VerificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CertificateRepository : JpaRepository<Certificate, Long> {
    fun findByStudentId(studentId: Long): List<Certificate>
    fun findByStudentIdAndVerificationStatus(studentId: Long, status: VerificationStatus): List<Certificate>

    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.verificationStatus = :status")
    fun countByStatus(@Param("status") status: VerificationStatus): Long
}

