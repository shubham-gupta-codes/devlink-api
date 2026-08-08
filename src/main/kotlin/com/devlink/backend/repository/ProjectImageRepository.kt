package com.devlink.backend.repository

import com.devlink.backend.entity.ProjectImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectImageRepository : JpaRepository<ProjectImage, Long>
