package com.devlink.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "saved_candidates", uniqueConstraints = [
    UniqueConstraint(columnNames = ["recruiter_id", "student_id", "job_id"])
])
class SavedCandidate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    val recruiter: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    val job: JobPosting? = null, // Optional job link, null represents general interest

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
