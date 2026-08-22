package com.devlink.backend.repository

import com.devlink.backend.entity.Conversation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ConversationRepository : JpaRepository<Conversation, Long> {
    fun findByTeamId(teamId: Long): Conversation?

    @Query("SELECT c FROM Conversation c " +
           "JOIN ConversationParticipant cp1 ON cp1.conversationId = c.id " +
           "JOIN ConversationParticipant cp2 ON cp2.conversationId = c.id " +
           "WHERE c.type = 'DIRECT' " +
           "AND cp1.userId = :user1 " +
           "AND cp2.userId = :user2")
    fun findDirectConversationBetween(
        @Param("user1") user1: Long,
        @Param("user2") user2: Long
    ): List<Conversation>
}
