package com.devlink.backend.repository

import com.devlink.backend.entity.Skill
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SkillRepository : JpaRepository<Skill, Long> {
    fun findByName(name: String): Optional<Skill>
}
