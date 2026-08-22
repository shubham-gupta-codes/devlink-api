package com.devlink.backend.controller

import com.devlink.backend.dto.*
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.TeamService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/teams")
class TeamController(
    private val teamService: TeamService
) {

    @PostMapping
    fun createTeam(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateTeamRequest
    ): ResponseEntity<TeamResponse> {
        return ResponseEntity.ok(teamService.createTeam(principal.id, request))
    }

    @PutMapping("/{teamId}")
    fun updateTeam(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable teamId: Long,
        @Valid @RequestBody request: UpdateTeamRequest
    ): ResponseEntity<TeamResponse> {
        return ResponseEntity.ok(teamService.updateTeam(principal.id, teamId, request))
    }

    @GetMapping("/{teamId}")
    fun getTeam(@PathVariable teamId: Long): ResponseEntity<TeamResponse> {
        return ResponseEntity.ok(teamService.getTeam(teamId))
    }

    @GetMapping("/my")
    fun getMyTeams(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<TeamResponse>> {
        return ResponseEntity.ok(teamService.getMyTeams(principal.id))
    }

    @PostMapping("/{teamId}/invite")
    fun inviteUser(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable teamId: Long,
        @RequestParam username: String
    ): ResponseEntity<TeamInviteResponse> {
        return ResponseEntity.ok(teamService.inviteUser(principal.id, teamId, username))
    }

    @PostMapping("/invites/{inviteId}/respond")
    fun respondToInvite(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable inviteId: Long,
        @RequestParam accept: Boolean
    ): ResponseEntity<Map<String, String>> {
        teamService.respondToInvite(principal.id, inviteId, accept)
        return ResponseEntity.ok(mapOf("message" to "Invitation processed successfully"))
    }

    @GetMapping("/invites/pending")
    fun getPendingInvites(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<TeamInviteResponse>> {
        return ResponseEntity.ok(teamService.getPendingInvites(principal.id))
    }
}
