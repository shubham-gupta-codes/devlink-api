package com.devlink.backend.service.impl

import com.devlink.backend.dto.*
import com.devlink.backend.entity.*
import com.devlink.backend.repository.*
import com.devlink.backend.service.ChatService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatServiceImpl(
    private val conversationRepository: ConversationRepository,
    private val conversationParticipantRepository: ConversationParticipantRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val recruiterProfileRepository: RecruiterProfileRepository
) : ChatService {

    override fun getInbox(userId: Long): List<ConversationResponse> {
        val participations = conversationParticipantRepository.findByUserId(userId)
        val convs = participations.mapNotNull { conversationRepository.findById(it.conversationId).orElse(null) }
        return convs.map { mapToConversationResponse(it, userId) }
            .sortedWith(compareByDescending<ConversationResponse> { it.lastMessageAt }.thenByDescending { it.id })
    }

    override fun getConversationMessages(conversationId: Long): List<MessageResponse> {
        val messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
        return messages.map { m ->
            MessageResponse(
                id = m.id!!,
                conversationId = conversationId,
                senderId = m.sender.id!!,
                senderUsername = m.sender.username,
                content = m.content,
                createdAt = m.createdAt
            )
        }
    }

    @Transactional
    override fun startDirectChat(userId: Long, targetUsername: String): ConversationResponse {
        val targetUser = userRepository.findByUsername(targetUsername)
            .orElseThrow { IllegalArgumentException("Target user not found") }

        if (userId == targetUser.id!!) {
            throw IllegalArgumentException("You cannot start a chat with yourself")
        }

        val existing = conversationRepository.findDirectConversationBetween(userId, targetUser.id!!)
        if (existing.isNotEmpty()) {
            return mapToConversationResponse(existing.first(), userId)
        }

        val conversation = Conversation(
            type = "DIRECT"
        )
        val saved = conversationRepository.save(conversation)

        val p1 = ConversationParticipant(conversationId = saved.id!!, userId = userId)
        val p2 = ConversationParticipant(conversationId = saved.id!!, userId = targetUser.id!!)
        conversationParticipantRepository.saveAll(listOf(p1, p2))

        return mapToConversationResponse(saved, userId)
    }

    private fun mapToConversationResponse(c: Conversation, currentUserId: Long): ConversationResponse {
        val participants = conversationParticipantRepository.findByConversationId(c.id!!)
        
        val pResponses = participants.map { p ->
            val user = userRepository.findById(p.userId).get()
            val name = studentProfileRepository.findById(p.userId).map { it.name }
                .orElseGet {
                    teacherProfileRepository.findById(p.userId).map { user.username }
                        .orElseGet {
                            recruiterProfileRepository.findById(p.userId).map { it.companyName }
                                .orElse(user.username)
                        }
                }
            ParticipantResponse(
                userId = p.userId,
                username = user.username,
                name = name
            )
        }

        val messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(c.id!!)
        val lastMsg = messages.lastOrNull()

        var resolvedName = ""
        var resolvedPhoto: String? = null

        if (c.type == "GROUP") {
            resolvedName = c.team?.name ?: "Group Chat"
            resolvedPhoto = c.team?.photoUrl
        } else {
            val other = pResponses.firstOrNull { it.userId != currentUserId }
            if (other != null) {
                resolvedName = other.name ?: other.username
                resolvedPhoto = studentProfileRepository.findById(other.userId).map { it.photoUrl }.orElseGet {
                    recruiterProfileRepository.findById(other.userId).map { it.companyLogo }.orElse(null)
                }
            } else {
                resolvedName = "Direct Chat"
            }
        }

        return ConversationResponse(
            id = c.id!!,
            type = c.type,
            teamId = c.team?.id,
            name = resolvedName,
            photoUrl = resolvedPhoto,
            lastMessagePreview = lastMsg?.content,
            lastMessageAt = lastMsg?.createdAt,
            participants = pResponses
        )
    }
}
