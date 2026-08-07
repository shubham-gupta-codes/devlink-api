package com.devlink.backend.entity

import com.devlink.backend.enums.SocialPlatform
import jakarta.persistence.*

@Entity
@Table(
    name = "social_links",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "platform"])]
)
class SocialLink(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var platform: SocialPlatform,

    @Column(nullable = false)
    var url: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
