package com.devlink.backend.entity

import com.devlink.backend.enums.JobStatus
import com.devlink.backend.enums.JobType
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "job_postings")
class JobPosting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    val recruiter: User,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    var jobType: JobType,

    @Column(name = "required_skills", nullable = false, length = 1000)
    var requiredSkills: String, // Comma-separated list of skills

    @Column(nullable = false)
    var location: String,

    @Column(name = "salary_range")
    var salaryRange: String? = null,

    @Column(nullable = false)
    var deadline: Instant,

    @Column(name = "start_date")
    var startDate: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: JobStatus = JobStatus.OPEN,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
