package com.devlink.backend.repository

import com.devlink.backend.entity.Certificate
import com.devlink.backend.enums.VerificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CertificateRepository : JpaRepository<Certificate, Long> {
    fun findByStudentId(studentId: Long): List<Certificate>
    fun findByStudentIdAndVerificationStatus(studentId: Long, status: VerificationStatus): List<Certificate>
}
