package com.devlink.backend.service

import com.devlink.backend.dto.ProfileResponse
import com.devlink.backend.dto.UpdateProfileRequest

interface ProfileService {
    fun getProfile(userId: Long): ProfileResponse
    fun getProfileByUsername(username: String): ProfileResponse
    fun updateProfile(userId: Long, request: UpdateProfileRequest): ProfileResponse
    fun getStudentRankings(): List<ProfileResponse>
}
