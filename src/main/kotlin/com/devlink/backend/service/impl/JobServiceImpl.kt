package com.devlink.backend.service.impl

import com.devlink.backend.dto.*
import com.devlink.backend.entity.*
import com.devlink.backend.enums.*
import com.devlink.backend.exception.*
import com.devlink.backend.repository.*
import com.devlink.backend.service.JobService
import com.devlink.backend.service.ProfileService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class JobServiceImpl(
    private val userRepository: UserRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val recruiterProfileRepository: RecruiterProfileRepository,
    private val jobRepository: JobRepository,
    private val jobApplicationRepository: JobApplicationRepository,
    private val savedCandidateRepository: SavedCandidateRepository,
    private val studentSkillRepository: StudentSkillRepository,
    private val profileService: ProfileService,
    private val notificationRepository: NotificationRepository,
    private val conversationRepository: ConversationRepository,
    private val conversationParticipantRepository: ConversationParticipantRepository,
    private val messageRepository: MessageRepository
) : JobService {

    override fun createJob(recruiterId: Long, request: CreateJobRequest): JobResponse {
        val recruiter = userRepository.findById(recruiterId)
            .orElseThrow { ProfileNotFoundException(recruiterId) }

        if (recruiter.role != Role.RECRUITER) {
            throw IllegalArgumentException("Only recruiters can post jobs")
        }

        val recruiterProfile = recruiterProfileRepository.findByUser(recruiter)
            .orElseThrow { ProfileNotFoundException(recruiterId) }

        val job = JobPosting(
            recruiter = recruiter,
            title = request.title,
            description = request.description,
            jobType = request.jobType,
            requiredSkills = request.requiredSkills.joinToString(", "),
            location = request.location,
            salaryRange = request.salaryRange,
            deadline = request.deadline,
            startDate = request.startDate,
            status = JobStatus.OPEN
        )

        val savedJob = jobRepository.save(job)
        return savedJob.toResponse(recruiterProfile.companyName, recruiterProfile.companyLogo)
    }

    override fun updateJob(recruiterId: Long, jobId: Long, request: UpdateJobRequest): JobResponse {
        val recruiter = userRepository.findById(recruiterId)
            .orElseThrow { ProfileNotFoundException(recruiterId) }

        val job = jobRepository.findById(jobId)
            .orElseThrow { IllegalArgumentException("Job not found: $jobId") }

        if (job.recruiter.id != recruiter.id && recruiter.role != Role.ADMIN) {
            throw IllegalArgumentException("You are not authorized to edit this job posting")
        }

        job.title = request.title
        job.description = request.description
        job.jobType = request.jobType
        job.requiredSkills = request.requiredSkills.joinToString(", ")
        job.location = request.location
        job.salaryRange = request.salaryRange
        job.deadline = request.deadline
        if (request.startDate != null) {
            job.startDate = request.startDate
        }
        if (request.status != null) {
            job.status = request.status
        }

        val updated = jobRepository.save(job)
        val recruiterProfile = recruiterProfileRepository.findByUser(job.recruiter).orElse(null)
        val count = jobApplicationRepository.countByJob(updated)
        return updated.toResponse(recruiterProfile?.companyName ?: "Unknown Company", recruiterProfile?.companyLogo, count)
    }

    override fun deleteJob(recruiterId: Long, jobId: Long) {
        val recruiter = userRepository.findById(recruiterId)
            .orElseThrow { ProfileNotFoundException(recruiterId) }

        val job = jobRepository.findById(jobId)
            .orElseThrow { IllegalArgumentException("Job not found: $jobId") }

        if (job.recruiter.id != recruiter.id && recruiter.role != Role.ADMIN) {
            throw IllegalArgumentException("You are not authorized to delete this job posting")
        }

        // Remove associations if any
        jobApplicationRepository.deleteAll(jobApplicationRepository.findAllByJob(job))
        savedCandidateRepository.deleteAll(savedCandidateRepository.findAllByJob(job))
        jobRepository.delete(job)
    }

    override fun getRecruiterJobs(recruiterId: Long): List<JobResponse> {
        val recruiter = userRepository.findById(recruiterId)
            .orElseThrow { ProfileNotFoundException(recruiterId) }

        val recruiterProfile = recruiterProfileRepository.findByUser(recruiter)
            .orElseThrow { ProfileNotFoundException(recruiterId) }

        return jobRepository.findAllByRecruiter(recruiter).map { job ->
            val count = jobApplicationRepository.countByJob(job)
            job.toResponse(recruiterProfile.companyName, recruiterProfile.companyLogo, count)
        }
    }

    override fun getAllOpenJobs(studentId: Long): List<JobResponse> {
        return jobRepository.findAllByStatus(JobStatus.OPEN).map { job ->
            val count = jobApplicationRepository.countByJob(job)
            val recruiter = job.recruiter
            val recruiterProfile = recruiterProfileRepository.findByUser(recruiter).orElse(null)
            val companyName = recruiterProfile?.companyName ?: "Unknown Company"
            val companyLogo = recruiterProfile?.companyLogo
            val hasApplied = userRepository.findById(studentId).map { student ->
                jobApplicationRepository.existsByJobAndStudent(job, student)
            }.orElse(false)

            job.toResponse(companyName, companyLogo, count, hasApplied)
        }
    }

    override fun getJobDetails(jobId: Long, userId: Long): JobResponse {
        val job = jobRepository.findById(jobId)
            .orElseThrow { IllegalArgumentException("Job not found: $jobId") }
        val recruiterProfile = recruiterProfileRepository.findByUser(job.recruiter).orElse(null)
        val companyName = recruiterProfile?.companyName ?: "Unknown Company"
        val companyLogo = recruiterProfile?.companyLogo
        val count = jobApplicationRepository.countByJob(job)
        val user = userRepository.findById(userId).orElse(null)
        val hasApplied = user?.let { jobApplicationRepository.existsByJobAndStudent(job, it) } ?: false

        return job.toResponse(companyName, companyLogo, count, hasApplied)
    }

    override fun applyToJob(studentId: Long, jobId: Long): JobApplicationResponse {
        val student = userRepository.findById(studentId)
            .orElseThrow { ProfileNotFoundException(studentId) }

        val job = jobRepository.findById(jobId)
            .orElseThrow { IllegalArgumentException("Job not found: $jobId") }

        if (jobApplicationRepository.existsByJobAndStudent(job, student)) {
            throw IllegalArgumentException("You have already applied to this job")
        }

        val application = JobApplication(
            job = job,
            student = student,
            status = JobApplicationStatus.PENDING
        )

        val savedApp = jobApplicationRepository.save(application)
        val studentProfile = profileService.getProfile(studentId)
        return savedApp.toResponse(studentProfile)
    }

    override fun getJobApplications(jobId: Long, recruiterId: Long): List<JobApplicationResponse> {
        val job = jobRepository.findById(jobId)
            .orElseThrow { IllegalArgumentException("Job not found: $jobId") }

        if (job.recruiter.id != recruiterId) {
            throw IllegalArgumentException("Access denied to job applicants")
        }

        return jobApplicationRepository.findAllByJob(job).map { app ->
            val studentProfile = profileService.getProfile(app.student.id!!)
            app.toResponse(studentProfile)
        }
    }

    override fun evaluateApplication(recruiterId: Long, applicationId: Long, status: JobApplicationStatus): JobApplicationResponse {
        val app = jobApplicationRepository.findById(applicationId)
            .orElseThrow { IllegalArgumentException("Application not found: $applicationId") }

        if (app.job.recruiter.id != recruiterId) {
            throw IllegalArgumentException("Access denied to evaluate application")
        }

        app.status = status
        val savedApp = jobApplicationRepository.save(app)

        // Save student notification
        val recruiterProfile = recruiterProfileRepository.findByUser(app.job.recruiter).orElse(null)
        val companyName = recruiterProfile?.companyName ?: "A recruiter"
        val statusVerb = when (status) {
            JobApplicationStatus.SHORTLISTED -> "shortlisted you for"
            JobApplicationStatus.REJECTED -> "rejected your application for"
            else -> "updated your status for"
        }
        val notification = Notification(
            user = app.student,
            title = "Job Application Status Update",
            message = "$companyName $statusVerb ${app.job.title}."
        )
        notificationRepository.save(notification)

        // Send chat message on shortlist
        if (status == JobApplicationStatus.SHORTLISTED) {
            sendShortlistChatMessage(
                recruiterId = recruiterId,
                studentId = app.student.id!!,
                messageContent = "Congratulations! You have been shortlisted by $companyName for the job: ${app.job.title}."
            )
        }

        val studentProfile = profileService.getProfile(app.student.id!!)
        return savedApp.toResponse(studentProfile)
    }

    override fun shortlistCandidate(recruiterId: Long, studentId: Long, jobId: Long?): SavedCandidateResponse {
        val recruiter = userRepository.findById(recruiterId)
            .orElseThrow { ProfileNotFoundException(recruiterId) }
        val student = userRepository.findById(studentId)
            .orElseThrow { ProfileNotFoundException(studentId) }
        val job = jobId?.let {
            jobRepository.findById(it).orElseThrow { IllegalArgumentException("Job not found: $it") }
        }

        if (savedCandidateRepository.existsByRecruiterAndStudentAndJob(recruiter, student, job)) {
            throw IllegalArgumentException("Candidate already shortlisted for this role")
        }

        val saved = SavedCandidate(
            recruiter = recruiter,
            student = student,
            job = job
        )
        val savedEntity = savedCandidateRepository.save(saved)

        val recruiterProfile = recruiterProfileRepository.findByUser(recruiter).orElse(null)
        val companyName = recruiterProfile?.companyName ?: "A recruiter"

        // Save student notification if linked to a job posting
        if (job != null) {
            val notification = Notification(
                user = student,
                title = "Job Shortlist Update",
                message = "$companyName shortlisted you for ${job.title}."
            )
            notificationRepository.save(notification)

            // Send chat message on shortlist
            sendShortlistChatMessage(
                recruiterId = recruiterId,
                studentId = studentId,
                messageContent = "Congratulations! You have been shortlisted by $companyName for the job: ${job.title}."
            )
        }

        val studentProfile = profileService.getProfile(studentId)
        return savedEntity.toResponse(studentProfile)
    }

    private fun sendShortlistChatMessage(recruiterId: Long, studentId: Long, messageContent: String) {
        val existing = conversationRepository.findDirectConversationBetween(recruiterId, studentId)
        val conversation = if (existing.isNotEmpty()) {
            existing.first()
        } else {
            val conv = Conversation(type = "DIRECT")
            val savedConv = conversationRepository.save(conv)
            val p1 = ConversationParticipant(conversationId = savedConv.id!!, userId = recruiterId)
            val p2 = ConversationParticipant(conversationId = savedConv.id!!, userId = studentId)
            conversationParticipantRepository.saveAll(listOf(p1, p2))
            savedConv
        }
        val sender = userRepository.findById(recruiterId).orElse(null) ?: return
        messageRepository.save(
            Message(
                conversation = conversation,
                sender = sender,
                content = messageContent
            )
        )
    }

    override fun getSavedCandidates(recruiterId: Long): List<SavedCandidateResponse> {
        val recruiter = userRepository.findById(recruiterId)
            .orElseThrow { ProfileNotFoundException(recruiterId) }

        return savedCandidateRepository.findAllByRecruiter(recruiter).map { saved ->
            val studentProfile = profileService.getProfile(saved.student.id!!)
            saved.toResponse(studentProfile)
        }
    }

    override fun getRecommendedCandidates(recruiterId: Long, jobId: Long): List<ProfileResponse> {
        val job = jobRepository.findById(jobId)
            .orElseThrow { IllegalArgumentException("Job not found: $jobId") }

        val reqSkillsList = job.requiredSkills.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        if (reqSkillsList.isEmpty()) {
            return emptyList()
        }

        val allStudents = studentProfileRepository.findAll()
        val recommendedList = mutableListOf<ProfileResponse>()

        for (student in allStudents) {
            val studentSkills = studentSkillRepository.findByStudent(student)
                .map { it.skill.name.lowercase().trim() }

            // If there's any match in skills
            val isMatch = studentSkills.any { s -> reqSkillsList.contains(s) }
            if (isMatch) {
                recommendedList.add(profileService.getProfile(student.id!!))
            }
        }

        return recommendedList.sortedByDescending { it.totalPoints ?: 0 }
    }

    override fun getStudentApplications(studentId: Long): List<JobApplicationResponse> {
        val student = userRepository.findById(studentId)
            .orElseThrow { ProfileNotFoundException(studentId) }
        val studentProfile = profileService.getProfile(studentId)
        return jobApplicationRepository.findAllByStudent(student).map { app ->
            app.toResponse(studentProfile)
        }
    }

    // Ext helpers
    private fun JobPosting.toResponse(companyName: String, companyLogo: String?, count: Long = 0, hasApplied: Boolean = false): JobResponse {
        return JobResponse(
            id = this.id!!,
            recruiterId = this.recruiter.id!!,
            recruiterCompanyName = companyName,
            recruiterCompanyLogo = companyLogo,
            title = this.title,
            description = this.description,
            jobType = this.jobType,
            requiredSkills = this.requiredSkills.split(",").map { it.trim() }.filter { it.isNotBlank() },
            location = this.location,
            salaryRange = this.salaryRange,
            deadline = this.deadline,
            startDate = this.startDate,
            status = this.status,
            createdAt = this.createdAt,
            applicantCount = count,
            hasApplied = hasApplied
        )
    }

    private fun JobApplication.toResponse(studentProfile: ProfileResponse): JobApplicationResponse {
        val recruiterProfile = recruiterProfileRepository.findByUser(this.job.recruiter).orElse(null)
        val companyName = recruiterProfile?.companyName ?: "Unknown Company"
        return JobApplicationResponse(
            id = this.id!!,
            jobId = this.job.id!!,
            jobTitle = this.job.title,
            recruiterCompanyName = companyName,
            studentProfile = studentProfile,
            status = this.status,
            createdAt = this.createdAt
        )
    }

    private fun SavedCandidate.toResponse(studentProfile: ProfileResponse): SavedCandidateResponse {
        return SavedCandidateResponse(
            id = this.id!!,
            studentProfile = studentProfile,
            jobId = this.job?.id,
            jobTitle = this.job?.title,
            createdAt = this.createdAt
        )
    }
}
