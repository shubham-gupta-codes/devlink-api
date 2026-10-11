package com.devlink.backend.service

import com.devlink.backend.dto.*
import com.devlink.backend.enums.Role

interface AdminService {
    fun getDashboardStats(): AdminDashboardStats
    fun listAllUsers(role: Role?, enabled: Boolean?): List<AdminUserListItem>
    fun suspendUser(userId: Long)
    fun reactivateUser(userId: Long)
    fun deleteUser(userId: Long)
    fun getPendingRecruiters(): List<PendingRecruiterResponse>
    fun approveRecruiter(userId: Long)
    fun rejectRecruiter(userId: Long, reason: String)
}
