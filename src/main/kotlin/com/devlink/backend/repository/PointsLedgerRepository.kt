package com.devlink.backend.repository

import com.devlink.backend.entity.PointsLedger
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PointsLedgerRepository : JpaRepository<PointsLedger, Long> {
    fun findByStudentId(studentId: Long): List<PointsLedger>
}
