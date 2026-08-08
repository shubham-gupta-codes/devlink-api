package com.devlink.backend.service

import com.devlink.backend.entity.Notification
import com.devlink.backend.entity.StudentBadge
import com.devlink.backend.entity.StudentProfile
import com.devlink.backend.enums.VerificationStatus
import com.devlink.backend.repository.NotificationRepository
import com.devlink.backend.repository.ProjectRepository
import com.devlink.backend.repository.StudentBadgeRepository
import com.devlink.backend.repository.StudentSkillRepository
import com.devlink.backend.repository.CertificateRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BadgeEvaluator(
    private val studentBadgeRepository: StudentBadgeRepository,
    private val projectRepository: ProjectRepository,
    private val studentSkillRepository: StudentSkillRepository,
    private val certificateRepository: CertificateRepository,
    private val notificationRepository: NotificationRepository
) {

    @Transactional
    fun evaluateBadges(student: StudentProfile) {
        val studentId = student.id!!

        // 1. Evaluate "Android Developer"
        val hasAndroidSkill = studentSkillRepository.findByStudent(student).any {
            val name = it.skill.name.lowercase()
            name.contains("kotlin") || name.contains("android") || name.contains("compose")
        }
        val approvedAndroidProjectsCount = projectRepository.findByStudentId(studentId).filter {
            it.verificationStatus == VerificationStatus.APPROVED && (
                it.techStack.lowercase().contains("kotlin") ||
                it.techStack.lowercase().contains("android") ||
                it.techStack.lowercase().contains("compose")
            )
        }.size
        if (hasAndroidSkill || approvedAndroidProjectsCount >= 2) {
            awardBadge(student, "Android Developer")
        }

        // 2. Evaluate "Top Contributor"
        val totalApprovedProjects = projectRepository.findByStudentId(studentId).filter {
            it.verificationStatus == VerificationStatus.APPROVED
        }.size
        val totalApprovedSkills = studentSkillRepository.findByStudent(student).size
        val totalPoints = student.totalPoints ?: 0
        if (totalPoints >= 100 || (totalApprovedProjects + totalApprovedSkills) >= 3) {
            awardBadge(student, "Top Contributor")
        }

        // 3. Evaluate "Hackathon Winner"
        val hasHackathonProject = projectRepository.findByStudentId(studentId).any {
            it.verificationStatus == VerificationStatus.APPROVED && (
                it.name.lowercase().contains("hackathon") ||
                it.description.lowercase().contains("hackathon")
            )
        }
        val hasHackathonCertificate = certificateRepository.findByStudentId(studentId).any {
            it.verificationStatus == VerificationStatus.APPROVED &&
                it.title.lowercase().contains("hackathon")
        }
        if (hasHackathonProject || hasHackathonCertificate) {
            awardBadge(student, "Hackathon Winner")
        }
    }

    private fun awardBadge(student: StudentProfile, badgeName: String) {
        if (!studentBadgeRepository.existsByStudentAndBadgeName(student, badgeName)) {
            val badge = StudentBadge(
                student = student,
                badgeName = badgeName
            )
            studentBadgeRepository.save(badge)

            // Send notification
            val notification = Notification(
                user = student.user,
                title = "New Badge Earned!",
                message = "You earned the '$badgeName' badge!"
            )
            notificationRepository.save(notification)
        }
    }
}
