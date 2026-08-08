package com.devlink.backend.entity

import com.devlink.backend.enums.FileType
import com.devlink.backend.enums.VerificationStatus
import jakarta.persistence.*

@Entity
@Table(name = "certificates")
class Certificate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentProfile,

    @Column(nullable = false)
    var title: String,

    @Column(name = "issuing_org", nullable = false)
    var issuingOrg: String,

    @Column(nullable = false, length = 1000)
    var purpose: String,

    @Column(name = "file_url", nullable = false, length = 512)
    var fileUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    var fileType: FileType,

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    var verificationStatus: VerificationStatus = VerificationStatus.PENDING
)
