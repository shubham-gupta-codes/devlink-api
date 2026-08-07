package com.devlink.backend.entity

import com.devlink.backend.enums.Course
import com.devlink.backend.enums.Branch
import com.devlink.backend.enums.AcademicStatus
import jakarta.persistence.*

@Entity
@Table(name = "student_profiles")
class StudentProfile(
    @Id
    @Column(name = "user_id")
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(nullable = false)
    var name: String,

    @Column(name = "photo_url")
    var photoUrl: String? = null,

    @Enumerated(EnumType.STRING)
    var course: Course? = null,

    @Enumerated(EnumType.STRING)
    var branch: Branch? = null,

    @Column(name = "admission_year")
    var admissionYear: Int? = null,

    var semester: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AcademicStatus = AcademicStatus.ACTIVE,

    @Column(length = 1000)
    var bio: String? = null,

    @Column(name = "total_points", nullable = false)
    var totalPoints: Int = 0,

    var contact: String? = null
)
