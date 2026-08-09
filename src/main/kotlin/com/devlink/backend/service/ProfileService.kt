package com.devlink.backend.service

import com.devlink.backend.dto.ProfileResponse
import com.devlink.backend.dto.UpdateProfileRequest
import com.devlink.backend.enums.Course
import com.devlink.backend.enums.Branch

interface ProfileService {
    fun getProfile(userId: Long): ProfileResponse
    fun getProfileByUsername(username: String): ProfileResponse
    fun updateProfile(userId: Long, request: UpdateProfileRequest): ProfileResponse
    fun getStudentRankings(course: Course?, branch: Branch?, admissionYear: Int?): List<ProfileResponse>
    fun searchStudents(
        query: String?,
        course: Course?,
        branch: Branch?,
        admissionYear: Int?,
        semester: Int?,
        skill: String?,
        badge: String?
    ): List<ProfileResponse>
}
