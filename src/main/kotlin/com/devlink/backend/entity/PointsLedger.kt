package com.devlink.backend.entity

import com.devlink.backend.enums.PointSource
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "points_ledger")
class PointsLedger(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentProfile,

    @Enumerated(EnumType.STRING)
    @Column(name = "point_source", nullable = false)
    val pointSource: PointSource,

    @Column(nullable = false)
    val points: Int,

    @Column(name = "reference_id", nullable = false)
    val referenceId: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
