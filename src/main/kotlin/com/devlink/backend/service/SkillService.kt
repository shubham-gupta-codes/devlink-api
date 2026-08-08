package com.devlink.backend.service

import com.devlink.backend.dto.*
import com.devlink.backend.enums.SkillLevel
import java.time.Instant

interface SkillService {
    fun getAllSkills(): List<SkillResponse>
    fun requestSkillVerification(studentId: Long, skillId: Long): SkillRequestResponse
    fun getStudentRequests(studentId: Long): List<SkillRequestResponse>
    fun getStudentApprovedSkills(studentId: Long): List<StudentSkillResponse>
    fun getTeacherPendingRequests(teacherId: Long): List<SkillRequestResponse>
    fun scheduleMeeting(teacherId: Long, requestId: Long, meetingDate: Instant, notes: String): SkillRequestResponse
    fun approveRequest(teacherId: Long, requestId: Long, level: SkillLevel, points: Int): SkillRequestResponse
    fun rejectRequest(teacherId: Long, requestId: Long, reason: String): SkillRequestResponse
}
