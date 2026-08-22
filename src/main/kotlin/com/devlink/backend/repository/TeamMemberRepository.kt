package com.devlink.backend.repository

import com.devlink.backend.entity.TeamMember
import com.devlink.backend.entity.TeamMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamMemberRepository : JpaRepository<TeamMember, TeamMemberId> {
    fun findByTeamId(teamId: Long): List<TeamMember>
    fun findByUserId(userId: Long): List<TeamMember>
    fun existsByTeamIdAndUserId(teamId: Long, userId: Long): Boolean
    fun deleteByTeamIdAndUserId(teamId: Long, userId: Long)
}
