package com.devlink.backend.repository

import com.devlink.backend.entity.JobApplication
import com.devlink.backend.entity.JobPosting
import com.devlink.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface JobApplicationRepository : JpaRepository<JobApplication, Long> {
    fun findAllByJob(job: JobPosting): List<JobApplication>
    fun findAllByStudent(student: User): List<JobApplication>
    fun findByJobAndStudent(job: JobPosting, student: User): Optional<JobApplication>
    fun countByJob(job: JobPosting): Long
    fun existsByJobAndStudent(job: JobPosting, student: User): Boolean
}
