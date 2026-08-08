package com.devlink.backend.repository

import com.devlink.backend.entity.ProjectLike
import com.devlink.backend.entity.ProjectLikeId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectLikeRepository : JpaRepository<ProjectLike, ProjectLikeId> {
    fun existsByProjectIdAndUserId(projectId: Long, userId: Long): Boolean
    fun findByProjectIdAndUserId(projectId: Long, userId: Long): ProjectLike?
}
