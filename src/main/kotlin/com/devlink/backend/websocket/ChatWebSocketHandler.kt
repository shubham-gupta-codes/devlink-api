package com.devlink.backend.websocket

import com.devlink.backend.entity.Message
import com.devlink.backend.repository.ConversationParticipantRepository
import com.devlink.backend.repository.ConversationRepository
import com.devlink.backend.repository.MessageRepository
import com.devlink.backend.repository.UserRepository
import com.devlink.backend.security.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class WsMessagePayload(
    val conversationId: Long,
    val content: String
)

@Component
class ChatWebSocketHandler(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository,
    private val conversationParticipantRepository: ConversationParticipantRepository,
    private val messageRepository: MessageRepository,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val userSessions = ConcurrentHashMap<Long, CopyOnWriteArrayList<WebSocketSession>>()
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = getUserIdFromSession(session)
        if (userId == null) {
            session.close(CloseStatus.BAD_DATA)
            return
        }
        userSessions.computeIfAbsent(userId) { CopyOnWriteArrayList() }.add(session)
    }

    /**
     * Inbound socket frames are intentionally ignored. Messages are persisted through the REST endpoint
     * (POST /api/chat/conversations/{id}/messages) and then pushed to participants via [broadcastMessage].
     * Persisting here as well used to store every message twice.
     */
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // no-op
    }

    /** Push an already-saved message to every connected session of every participant in the conversation. */
    fun broadcastMessage(message: com.devlink.backend.dto.MessageResponse) {
        try {
            val participants = conversationParticipantRepository.findByConversationId(message.conversationId)

            val responseMap = mapOf(
                "id" to message.id,
                "conversationId" to message.conversationId,
                "senderId" to message.senderId,
                "senderUsername" to message.senderUsername,
                "content" to message.content,
                "createdAt" to message.createdAt.toString()
            )
            val jsonMsg = TextMessage(objectMapper.writeValueAsString(responseMap))

            for (part in participants) {
                val sessions = userSessions[part.userId] ?: continue
                for (s in sessions) {
                    if (s.isOpen) {
                        synchronized(s) { s.sendMessage(jsonMsg) }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = getUserIdFromSession(session) ?: return
        userSessions[userId]?.remove(session)
        if (userSessions[userId]?.isEmpty() == true) {
            userSessions.remove(userId)
        }
    }

    private fun getUserIdFromSession(session: WebSocketSession): Long? {
        val query = session.uri?.query ?: return null
        val tokenParam = query.split("&").firstOrNull { it.startsWith("token=") } ?: return null
        val token = tokenParam.substringAfter("token=")
        return try {
            if (jwtService.isTokenValid(token)) {
                jwtService.extractUserId(token)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
