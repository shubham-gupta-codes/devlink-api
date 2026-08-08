package com.devlink.backend.repository

import com.devlink.backend.entity.ProjectComment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectCommentRepository : JpaRepository<ProjectComment, Long> {
    fun findByProjectIdAndParentCommentIsNullOrderByCreatedAtAsc(projectId: Long): List<ProjectComment>
}
