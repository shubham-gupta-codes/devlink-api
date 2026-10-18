package com.devlink.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "stored_files")
class StoredFile(

    @Column(nullable = false, unique = true)
    var filename: String,

    @Column(name = "content_type", nullable = false)
    var contentType: String,

    @Lob
    @Column(nullable = false)
    var data: ByteArray,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
