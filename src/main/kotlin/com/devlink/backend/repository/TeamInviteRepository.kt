package com.devlink.backend.repository

import com.devlink.backend.entity.TeamInvite
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamInviteRepository : JpaRepository<TeamInvite, Long> {
    fun findByInvitedUserIdAndStatus(invitedUserId: Long, status: String): List<TeamInvite>
    fun existsByTeamIdAndInvitedUserIdAndStatus(teamId: Long, invitedUserId: Long, status: String): Boolean
}
