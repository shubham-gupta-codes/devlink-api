package com.devlink.backend.repository

import com.devlink.backend.entity.VerificationRequest
import com.devlink.backend.enums.EntityType
import com.devlink.backend.enums.VerificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VerificationRequestRepository : JpaRepository<VerificationRequest, Long> {
    fun findByReviewedByIdAndStatus(reviewedById: Long, status: VerificationStatus): List<VerificationRequest>
    
    fun findByReviewedByIdAndEntityTypeAndStatus(reviewedById: Long, entityType: EntityType, status: VerificationStatus): List<VerificationRequest>

    fun findByEntityTypeAndEntityId(entityType: EntityType, entityId: Long): VerificationRequest?

    fun countByReviewedById(reviewedById: Long): Long
}
