package com.devlink.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "student_badges",
    uniqueConstraints = [UniqueConstraint(columnNames = ["student_id", "badge_name"])]
)
class StudentBadge(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentProfile,

    @Column(name = "badge_name", nullable = false, length = 100)
    val badgeName: String,

    @Column(name = "earned_at", nullable = false)
    val earnedAt: Instant = Instant.now()
)
