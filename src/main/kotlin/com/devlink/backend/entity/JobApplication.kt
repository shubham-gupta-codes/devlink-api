package com.devlink.backend.entity

import com.devlink.backend.enums.JobApplicationStatus
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "job_applications", uniqueConstraints = [
    UniqueConstraint(columnNames = ["job_id", "student_id"])
])
class JobApplication(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    val job: JobPosting,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: JobApplicationStatus = JobApplicationStatus.PENDING,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
