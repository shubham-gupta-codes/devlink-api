package com.devlink.backend.service

import com.devlink.backend.dto.*

interface TeamService {
    fun createTeam(creatorId: Long, request: CreateTeamRequest): TeamResponse
    fun updateTeam(userId: Long, teamId: Long, request: UpdateTeamRequest): TeamResponse
    fun getTeam(teamId: Long): TeamResponse
    fun getMyTeams(userId: Long): List<TeamResponse>
    fun inviteUser(senderId: Long, teamId: Long, invitedUsername: String): TeamInviteResponse
    fun respondToInvite(userId: Long, inviteId: Long, accept: Boolean)
    fun getPendingInvites(userId: Long): List<TeamInviteResponse>
}
