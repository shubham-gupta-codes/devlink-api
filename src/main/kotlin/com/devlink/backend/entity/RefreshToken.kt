package com.devlink.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false, unique = true, length = 512)
    var token: String,

    @Column(name = "expired_at", nullable = false)
    var expiredAt: Instant,

    @Column(nullable = false)
    var revoked: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    fun isExpired(): Boolean = Instant.now().isAfter(expiredAt)
}
