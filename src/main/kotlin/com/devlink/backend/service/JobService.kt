package com.devlink.backend.service

import com.devlink.backend.dto.*
import com.devlink.backend.enums.JobApplicationStatus

interface JobService {
    fun createJob(recruiterId: Long, request: CreateJobRequest): JobResponse
    fun updateJob(recruiterId: Long, jobId: Long, request: UpdateJobRequest): JobResponse
    fun deleteJob(recruiterId: Long, jobId: Long)
    fun getRecruiterJobs(recruiterId: Long): List<JobResponse>
    fun getAllOpenJobs(studentId: Long): List<JobResponse>
    fun getJobDetails(jobId: Long, userId: Long): JobResponse
    
    fun applyToJob(studentId: Long, jobId: Long): JobApplicationResponse
    fun getJobApplications(jobId: Long, recruiterId: Long): List<JobApplicationResponse>
    fun getStudentApplications(studentId: Long): List<JobApplicationResponse>
    
    fun evaluateApplication(recruiterId: Long, applicationId: Long, status: JobApplicationStatus): JobApplicationResponse
    
    fun shortlistCandidate(recruiterId: Long, studentId: Long, jobId: Long?): SavedCandidateResponse
    fun getSavedCandidates(recruiterId: Long): List<SavedCandidateResponse>
    
    fun getRecommendedCandidates(recruiterId: Long, jobId: Long): List<ProfileResponse>
}
