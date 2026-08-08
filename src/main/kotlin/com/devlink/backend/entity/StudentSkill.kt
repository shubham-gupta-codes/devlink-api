package com.devlink.backend.entity

import com.devlink.backend.enums.SkillLevel
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "student_skills",
    uniqueConstraints = [UniqueConstraint(columnNames = ["student_id", "skill_id"])]
)
class StudentSkill(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    val skill: Skill,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var level: SkillLevel,

    @Column(name = "points_awarded", nullable = false)
    var pointsAwarded: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    var approvedBy: TeacherProfile? = null,

    @Column(name = "approved_at", nullable = false)
    val approvedAt: Instant = Instant.now()
)
