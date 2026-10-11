package com.devlink.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "teacher_feedbacks")
class TeacherFeedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    val teacher: TeacherProfile,

    @Column(nullable = false, length = 2000)
    var content: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
