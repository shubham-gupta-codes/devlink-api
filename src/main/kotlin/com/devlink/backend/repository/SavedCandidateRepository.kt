package com.devlink.backend.repository

import com.devlink.backend.entity.JobPosting
import com.devlink.backend.entity.SavedCandidate
import com.devlink.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SavedCandidateRepository : JpaRepository<SavedCandidate, Long> {
    fun findAllByRecruiter(recruiter: User): List<SavedCandidate>
    fun findAllByJob(job: JobPosting): List<SavedCandidate>
    fun findByRecruiterAndStudentAndJob(recruiter: User, student: User, job: JobPosting?): Optional<SavedCandidate>
    fun existsByRecruiterAndStudentAndJob(recruiter: User, student: User, job: JobPosting?): Boolean
}
