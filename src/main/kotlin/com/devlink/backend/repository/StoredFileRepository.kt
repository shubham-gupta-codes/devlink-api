package com.devlink.backend.repository

import com.devlink.backend.entity.StoredFile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface StoredFileRepository : JpaRepository<StoredFile, Long> {
    fun findByFilename(filename: String): Optional<StoredFile>
    fun existsByFilename(filename: String): Boolean
}
