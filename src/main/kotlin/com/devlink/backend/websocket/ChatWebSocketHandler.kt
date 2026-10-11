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

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val senderId = getUserIdFromSession(session) ?: return
        try {
            val payload = objectMapper.readValue(message.payload, WsMessagePayload::class.java)
            val conversation = conversationRepository.findById(payload.conversationId).orElse(null) ?: return
            
            val isParticipant = conversationParticipantRepository.existsByConversationIdAndUserId(payload.conversationId, senderId)
            if (!isParticipant) return

            val sender = userRepository.findById(senderId).orElse(null) ?: return

            val savedMsg = Message(
                conversation = conversation,
                sender = sender,
                content = payload.content
            )
            messageRepository.save(savedMsg)

            val participants = conversationParticipantRepository.findByConversationId(payload.conversationId)
            
            val responseMap = mapOf(
                "id" to savedMsg.id,
                "conversationId" to payload.conversationId,
                "senderId" to sender.id,
                "senderUsername" to sender.username,
                "content" to savedMsg.content,
                "createdAt" to savedMsg.createdAt.toString()
            )
            val jsonMsg = TextMessage(objectMapper.writeValueAsString(responseMap))

            for (part in participants) {
                val sessions = userSessions[part.userId] ?: continue
                for (s in sessions) {
                    if (s.isOpen) {
                        s.sendMessage(jsonMsg)
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
