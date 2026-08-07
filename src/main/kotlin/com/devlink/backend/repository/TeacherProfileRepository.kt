package com.devlink.backend.repository

import com.devlink.backend.entity.TeacherProfile
import com.devlink.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface TeacherProfileRepository : JpaRepository<TeacherProfile, Long> {
    fun findByUser(user: User): Optional<TeacherProfile>
    fun findByUserUsername(username: String): Optional<TeacherProfile>
}
