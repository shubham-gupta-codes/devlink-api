package com.devlink.backend.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

data class ProjectLikeId(
    val projectId: Long = 0L,
    val userId: Long = 0L
) : Serializable

@Entity
@Table(name = "project_likes")
@IdClass(ProjectLikeId::class)
class ProjectLike(
    @Id
    @Column(name = "project_id")
    val projectId: Long,

    @Id
    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
