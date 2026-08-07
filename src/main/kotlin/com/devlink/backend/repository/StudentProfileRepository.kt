package com.devlink.backend.repository

import com.devlink.backend.entity.StudentProfile
import com.devlink.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface StudentProfileRepository : JpaRepository<StudentProfile, Long> {
    fun findByUser(user: User): Optional<StudentProfile>
    fun findByUserUsername(username: String): Optional<StudentProfile>
}
