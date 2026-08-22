package com.devlink.backend.controller

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@RestController
@RequestMapping("/api/files")
class FileController {

    private val uploadDir = Paths.get("uploads")

    init {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }
    }

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty")
        }

        val originalFilename = file.originalFilename ?: "file"
        val extension = originalFilename.substringAfterLast('.', "")
        val newFilename = "${UUID.randomUUID()}${if (extension.isNotBlank()) ".$extension" else ""}"
        val targetPath = uploadDir.resolve(newFilename)

        Files.copy(file.inputStream, targetPath)

        val downloadUrl = "/api/files/download/$newFilename"
        return ResponseEntity.ok(mapOf("url" to downloadUrl, "filename" to newFilename))
    }

    @GetMapping("/download/{filename:.+}")
    fun downloadFile(@PathVariable filename: String): ResponseEntity<Resource> {
        val filePath = uploadDir.resolve(filename).normalize()
        val resource = UrlResource(filePath.toUri())

        if (!resource.exists() || !resource.isReadable) {
            return ResponseEntity.notFound().build()
        }

        val contentType = try {
            Files.probeContentType(filePath) ?: "application/octet-stream"
        } catch (e: Exception) {
            "application/octet-stream"
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
            .body(resource)
    }
}
