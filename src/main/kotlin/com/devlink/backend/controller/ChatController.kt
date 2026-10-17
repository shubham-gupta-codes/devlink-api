package com.devlink.backend.controller

import com.devlink.backend.dto.*
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.ChatService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
    private val chatWebSocketHandler: com.devlink.backend.websocket.ChatWebSocketHandler
) {

    @GetMapping("/inbox")
    fun getInbox(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<ConversationResponse>> {
        return ResponseEntity.ok(chatService.getInbox(principal.id))
    }

    @GetMapping("/conversations/{conversationId}/messages")
    fun getMessages(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable conversationId: Long
    ): ResponseEntity<List<MessageResponse>> {
        return ResponseEntity.ok(chatService.getConversationMessages(principal.id, conversationId))
    }

    @PostMapping("/conversations/direct")
    fun startDirectChat(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam targetUsername: String
    ): ResponseEntity<ConversationResponse> {
        return ResponseEntity.ok(chatService.startDirectChat(principal.id, targetUsername))
    }

    @PostMapping("/conversations/{conversationId}/messages")
    fun sendMessage(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable conversationId: Long,
        @RequestBody request: SendMessageRequest
    ): ResponseEntity<MessageResponse> {
        val saved = chatService.sendMessage(principal.id, conversationId, request.content)
        chatWebSocketHandler.broadcastMessage(saved)
        return ResponseEntity.ok(saved)
    }
}
