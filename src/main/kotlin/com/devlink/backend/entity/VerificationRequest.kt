package com.devlink.backend.entity

import com.devlink.backend.enums.EntityType
import com.devlink.backend.enums.VerificationStatus
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "verification_requests")
class VerificationRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    val entityType: EntityType,

    @Column(name = "entity_id", nullable = false)
    val entityId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    val requestedBy: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: VerificationStatus = VerificationStatus.PENDING,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", nullable = false)
    var reviewedBy: User,

    @Column(name = "reviewed_at")
    var reviewedAt: Instant? = null,

    @Column(length = 1000)
    var remarks: String? = null,

    @Column(name = "schedule_at")
    var scheduleAt: Instant? = null
)
