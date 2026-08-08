package com.devlink.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "project_images")
class ProjectImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    val project: Project,

    @Column(name = "image_url", nullable = false, length = 512)
    var imageUrl: String
)
