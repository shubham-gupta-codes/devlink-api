package com.devlink.backend.service.impl

import com.devlink.backend.dto.*
import com.devlink.backend.entity.*
import com.devlink.backend.repository.*
import com.devlink.backend.service.TeamService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TeamServiceImpl(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamInviteRepository: TeamInviteRepository,
    private val conversationRepository: ConversationRepository,
    private val conversationParticipantRepository: ConversationParticipantRepository,
    private val userRepository: UserRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val recruiterProfileRepository: RecruiterProfileRepository,
    private val notificationRepository: NotificationRepository
) : TeamService {

    @Transactional
    override fun createTeam(creatorId: Long, request: CreateTeamRequest): TeamResponse {
        val creator = userRepository.findById(creatorId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val team = Team(
            name = request.name,
            description = request.description,
            photoUrl = request.photoUrl,
            createdBy = creator
        )
        val savedTeam = teamRepository.save(team)

        val member = TeamMember(
            teamId = savedTeam.id!!,
            userId = creatorId,
            role = "ADMIN"
        )
        teamMemberRepository.save(member)

        val conversation = Conversation(
            type = "GROUP",
            team = savedTeam
        )
        val savedConv = conversationRepository.save(conversation)

        val cp = ConversationParticipant(
            conversationId = savedConv.id!!,
            userId = creatorId
        )
        conversationParticipantRepository.save(cp)

        return mapToTeamResponse(savedTeam)
    }

    @Transactional
    override fun updateTeam(userId: Long, teamId: Long, request: UpdateTeamRequest): TeamResponse {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }

        val isAdmin = teamMemberRepository.existsByTeamIdAndUserId(teamId, userId) && 
                      teamMemberRepository.findById(TeamMemberId(teamId, userId)).get().role == "ADMIN"
        if (!isAdmin) {
            throw SecurityException("Only admins can edit team settings")
        }

        team.name = request.name
        team.description = request.description
        team.photoUrl = request.photoUrl
        val savedTeam = teamRepository.save(team)

        return mapToTeamResponse(savedTeam)
    }

    override fun getTeam(teamId: Long): TeamResponse {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }
        return mapToTeamResponse(team)
    }

    override fun getMyTeams(userId: Long): List<TeamResponse> {
        val memberships = teamMemberRepository.findByUserId(userId)
        return memberships.mapNotNull { m ->
            teamRepository.findById(m.teamId).map { mapToTeamResponse(it) }.orElse(null)
        }
    }

    @Transactional
    override fun inviteUser(senderId: Long, teamId: Long, invitedUsername: String): TeamInviteResponse {
        val team = teamRepository.findById(teamId)
            .orElseThrow { IllegalArgumentException("Team not found") }
        val sender = userRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("Sender not found") }
        val invitedUser = userRepository.findByUsername(invitedUsername)
            .orElseThrow { IllegalArgumentException("Invited user not found") }

        val isAdmin = teamMemberRepository.existsByTeamIdAndUserId(teamId, senderId) && 
                      teamMemberRepository.findById(TeamMemberId(teamId, senderId)).get().role == "ADMIN"
        if (!isAdmin) {
            throw SecurityException("Only admins can invite members")
        }

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, invitedUser.id!!)) {
            throw IllegalArgumentException("User is already a member of this team")
        }

        if (teamInviteRepository.existsByTeamIdAndInvitedUserIdAndStatus(teamId, invitedUser.id!!, "PENDING")) {
            throw IllegalArgumentException("An invite is already pending for this user")
        }

        val invite = TeamInvite(
            team = team,
            invitedUser = invitedUser,
            invitedBy = sender,
            status = "PENDING"
        )
        val savedInvite = teamInviteRepository.save(invite)

        val notification = Notification(
            user = invitedUser,
            title = "Team Invitation",
            message = "@${sender.username} invited you in the group '${team.name}'"
        )
        notificationRepository.save(notification)

        return mapToInviteResponse(savedInvite)
    }

    @Transactional
    override fun respondToInvite(userId: Long, inviteId: Long, accept: Boolean) {
        val invite = teamInviteRepository.findById(inviteId)
            .orElseThrow { IllegalArgumentException("Invite not found") }

        if (invite.invitedUser.id != userId) {
            throw SecurityException("You cannot respond to this invite")
        }

        if (invite.status != "PENDING") {
            throw IllegalArgumentException("Invite has already been processed")
        }

        if (accept) {
            invite.status = "ACCEPTED"
            val member = TeamMember(
                teamId = invite.team.id!!,
                userId = userId,
                role = "MEMBER"
            )
            teamMemberRepository.save(member)

            val conversation = conversationRepository.findByTeamId(invite.team.id!!)
            if (conversation != null) {
                val participant = ConversationParticipant(
                    conversationId = conversation.id!!,
                    userId = userId
                )
                conversationParticipantRepository.save(participant)
            }

            val notification = Notification(
                user = invite.invitedBy,
                title = "Invitation Accepted",
                message = "@${invite.invitedUser.username} accepted your invitation to join '${invite.team.name}'"
            )
            notificationRepository.save(notification)
        } else {
            invite.status = "REJECTED"

            val notification = Notification(
                user = invite.invitedBy,
                title = "Invitation Rejected",
                message = "@${invite.invitedUser.username} rejected your invitation to join '${invite.team.name}'"
            )
            notificationRepository.save(notification)
        }
        teamInviteRepository.save(invite)
    }

    override fun getPendingInvites(userId: Long): List<TeamInviteResponse> {
        val invites = teamInviteRepository.findByInvitedUserIdAndStatus(userId, "PENDING")
        return invites.map { mapToInviteResponse(it) }
    }

    private fun mapToTeamResponse(team: Team): TeamResponse {
        val members = teamMemberRepository.findByTeamId(team.id!!)
        val memberResponses = members.map { m ->
            val user = userRepository.findById(m.userId).get()
            val name = studentProfileRepository.findById(m.userId).map { it.name }
                .orElseGet {
                    teacherProfileRepository.findById(m.userId).map { user.username }
                        .orElseGet {
                            recruiterProfileRepository.findById(m.userId).map { it.companyName }
                                .orElse(user.username)
                        }
                }
            TeamMemberResponse(
                userId = m.userId,
                username = user.username,
                name = name,
                role = m.role,
                joinedAt = m.joinedAt
            )
        }
        return TeamResponse(
            id = team.id!!,
            name = team.name,
            description = team.description,
            photoUrl = team.photoUrl,
            createdByUserId = team.createdBy.id!!,
            createdByUsername = team.createdBy.username,
            createdAt = team.createdAt,
            members = memberResponses
        )
    }

    private fun mapToInviteResponse(invite: TeamInvite): TeamInviteResponse {
        return TeamInviteResponse(
            id = invite.id!!,
            teamId = invite.team.id!!,
            teamName = invite.team.name,
            invitedUserId = invite.invitedUser.id!!,
            invitedUsername = invite.invitedUser.username,
            invitedByUserId = invite.invitedBy.id!!,
            invitedByUsername = invite.invitedBy.username,
            status = invite.status,
            createdAt = invite.createdAt
        )
    }
}
