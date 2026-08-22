package com.devlink.backend.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

data class TeamMemberId(
    val teamId: Long = 0L,
    val userId: Long = 0L
) : Serializable

@Entity
@Table(name = "team_members")
@IdClass(TeamMemberId::class)
class TeamMember(
    @Id
    @Column(name = "team_id")
    val teamId: Long,

    @Id
    @Column(name = "user_id")
    val userId: Long,

    @Column(nullable = false)
    var role: String, // ADMIN, MEMBER

    @Column(name = "joined_at", nullable = false)
    val joinedAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "team_id", referencedColumnName = "id", insertable = false, updatable = false)
    )
    var team: Team? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    )
    var user: User? = null
)
