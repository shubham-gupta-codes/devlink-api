package com.devlink.backend.repository

import com.devlink.backend.entity.ConversationParticipant
import com.devlink.backend.entity.ConversationParticipantId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConversationParticipantRepository : JpaRepository<ConversationParticipant, ConversationParticipantId> {
    fun findByConversationId(conversationId: Long): List<ConversationParticipant>
    fun findByUserId(userId: Long): List<ConversationParticipant>
    fun existsByConversationIdAndUserId(conversationId: Long, userId: Long): Boolean
}
