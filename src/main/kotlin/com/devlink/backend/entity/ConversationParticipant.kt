package com.devlink.backend.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

data class ConversationParticipantId(
    val conversationId: Long = 0L,
    val userId: Long = 0L
) : Serializable

@Entity
@Table(name = "conversation_participants")
@IdClass(ConversationParticipantId::class)
class ConversationParticipant(
    @Id
    @Column(name = "conversation_id")
    val conversationId: Long,

    @Id
    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "conversation_id", referencedColumnName = "id", insertable = false, updatable = false)
    )
    var conversation: Conversation? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    )
    var user: User? = null
)
