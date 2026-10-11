package com.devlink.backend.repository

import com.devlink.backend.entity.JobPosting
import com.devlink.backend.entity.User
import com.devlink.backend.enums.JobStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobRepository : JpaRepository<JobPosting, Long> {
    fun findAllByRecruiter(recruiter: User): List<JobPosting>
    fun findAllByStatus(status: JobStatus): List<JobPosting>
}
