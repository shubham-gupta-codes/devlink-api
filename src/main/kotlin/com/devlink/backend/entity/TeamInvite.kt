package com.devlink.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "team_invites")
class TeamInvite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: Team,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id", nullable = false)
    var invitedUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    var invitedBy: User,

    @Column(nullable = false)
    var status: String = "PENDING", // PENDING, ACCEPTED, REJECTED

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
