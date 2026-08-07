package com.devlink.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "teacher_profiles")
class TeacherProfile(
    @Id
    @Column(name = "user_id")
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(name = "employee_id")
    var employeeId: Long? = null,

    @Column(nullable = false)
    var department: String,

    var designation: String? = null
)
