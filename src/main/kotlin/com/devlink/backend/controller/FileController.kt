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
    fun downloadFile(
        @PathVariable filename: String,
        @RequestParam(value = "name", required = false) displayName: String?
    ): ResponseEntity<Resource> {
        val filePath = uploadDir.resolve(filename).normalize()
        val resource = UrlResource(filePath.toUri())

        if (!resource.exists() || !resource.isReadable) {
            return ResponseEntity.notFound().build()
        }

        val ext = filename.substringAfterLast('.', "").lowercase()
        val mediaType = when (ext) {
            "pdf" -> MediaType.APPLICATION_PDF
            "png" -> MediaType.IMAGE_PNG
            "jpg", "jpeg" -> MediaType.IMAGE_JPEG
            "gif" -> MediaType.IMAGE_GIF
            "webp" -> MediaType.parseMediaType("image/webp")
            "svg" -> MediaType.parseMediaType("image/svg+xml")
            "txt" -> MediaType.TEXT_PLAIN
            else -> try {
                MediaType.parseMediaType(Files.probeContentType(filePath) ?: "application/octet-stream")
            } catch (e: Exception) {
                MediaType.APPLICATION_OCTET_STREAM
            }
        }

        val finalDownloadName = if (!displayName.isNullOrBlank()) {
            val clean = displayName.replace(Regex("[^a-zA-Z0-9._ -]"), "_")
            if (!clean.endsWith(".$ext", ignoreCase = true) && ext.isNotBlank()) {
                "$clean.$ext"
            } else {
                clean
            }
        } else {
            filename
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$finalDownloadName\"")
            .body(resource)
    }
}
