package com.devlink.backend.service

import com.devlink.backend.dto.*

interface ChatService {
    fun getInbox(userId: Long): List<ConversationResponse>
    fun getConversationMessages(conversationId: Long): List<MessageResponse>
    fun startDirectChat(userId: Long, targetUsername: String): ConversationResponse
    fun sendMessage(userId: Long, conversationId: Long, content: String): MessageResponse
}
