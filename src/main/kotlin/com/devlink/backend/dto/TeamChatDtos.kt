package com.devlink.backend.dto

import java.time.Instant

// Teams
data class CreateTeamRequest(
    val name: String,
    val description: String?,
    val photoUrl: String?
)

data class UpdateTeamRequest(
    val name: String,
    val description: String?,
    val photoUrl: String?
)

data class TeamResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val photoUrl: String?,
    val createdByUserId: Long,
    val createdByUsername: String,
    val createdAt: Instant,
    val members: List<TeamMemberResponse>
)

data class TeamMemberResponse(
    val userId: Long,
    val username: String,
    val name: String?,
    val role: String, // ADMIN, MEMBER
    val joinedAt: Instant
)

data class TeamInviteResponse(
    val id: Long,
    val teamId: Long,
    val teamName: String,
    val invitedUserId: Long,
    val invitedUsername: String,
    val invitedByUserId: Long,
    val invitedByUsername: String,
    val status: String,
    val createdAt: Instant
)

// Chats
data class ConversationResponse(
    val id: Long,
    val type: String, // DIRECT, GROUP
    val teamId: Long?,
    val name: String, // Resolves name (team name, other participant name, or recruiter name)
    val photoUrl: String?, // Avatar url
    val lastMessagePreview: String?,
    val lastMessageAt: Instant?,
    val participants: List<ParticipantResponse>
)

data class ParticipantResponse(
    val userId: Long,
    val username: String,
    val name: String?
)

data class MessageResponse(
    val id: Long,
    val conversationId: Long,
    val senderId: Long,
    val senderUsername: String,
    val content: String,
    val createdAt: Instant
)
