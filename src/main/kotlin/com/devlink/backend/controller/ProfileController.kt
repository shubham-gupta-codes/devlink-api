package com.devlink.backend.controller

import com.devlink.backend.dto.ProfileResponse
import com.devlink.backend.dto.UpdateProfileRequest
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.ProfileService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user/profile")
class ProfileController(
    private val profileService: ProfileService
) {

    /** GET /api/user/profile - Get current user's profile. */
    @GetMapping
    fun getMyProfile(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<ProfileResponse> {
        val profile = profileService.getProfile(principal.id)
        return ResponseEntity.ok(profile)
    }

    /** GET /api/user/profile/{userId} - Get profile by user ID. */
    @GetMapping("/{userId}")
    fun getProfileById(@PathVariable userId: Long): ResponseEntity<ProfileResponse> {
        val profile = profileService.getProfile(userId)
        return ResponseEntity.ok(profile)
    }

    /** GET /api/user/profile/username/{username} - Get profile by username. */
    @GetMapping("/username/{username}")
    fun getProfileByUsername(@PathVariable username: String): ResponseEntity<ProfileResponse> {
        val profile = profileService.getProfileByUsername(username)
        return ResponseEntity.ok(profile)
    }

    /** PUT /api/user/profile - Update current user's profile. */
    @PutMapping
    fun updateProfile(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<ProfileResponse> {
        val updated = profileService.updateProfile(principal.id, request)
        return ResponseEntity.ok(updated)
    }

    /** GET /api/user/profile/rankings - Get student leaderboard rankings. */
    @GetMapping("/rankings")
    fun getStudentRankings(): ResponseEntity<List<ProfileResponse>> {
        return ResponseEntity.ok(profileService.getStudentRankings())
    }
}
