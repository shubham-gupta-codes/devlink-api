package com.devlink.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "recruiter_profiles")
class RecruiterProfile(
    @Id
    @Column(name = "user_id")
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(name = "company_name", nullable = false)
    var companyName: String,

    @Column(name = "company_logo")
    var companyLogo: String? = null,

    var designation: String? = null,

    var contact: String? = null
)
