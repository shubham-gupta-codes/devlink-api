package com.devlink.backend.entity

import com.devlink.backend.enums.SkillStatus
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "skill_requests")
class SkillRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    val skill: Skill,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    var teacher: TeacherProfile,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SkillStatus = SkillStatus.PENDING,

    @Column(name = "meeting_date")
    var meetingDate: Instant? = null,

    @Column(name = "meeting_notes", length = 1000)
    var meetingNotes: String? = null,

    @Column(name = "rejection_reason", length = 1000)
    var rejectionReason: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
