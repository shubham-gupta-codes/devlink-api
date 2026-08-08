package com.devlink.backend.entity

import com.devlink.backend.enums.VerificationStatus
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "projects")
class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentProfile,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, length = 2000)
    var description: String,

    @Column(name = "tech_stack", nullable = false, length = 1000)
    var techStack: String,

    @Column(name = "github_url", length = 512)
    var githubUrl: String? = null,

    @Column(name = "demo_url", length = 512)
    var demoUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    var verificationStatus: VerificationStatus = VerificationStatus.PENDING,

    @Column(name = "like_count", nullable = false)
    var likeCount: Long = 0,

    @Column(name = "comment_count", nullable = false)
    var commentCount: Long = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<ProjectImage> = mutableListOf()
}
