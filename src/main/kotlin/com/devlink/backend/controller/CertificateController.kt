package com.devlink.backend.controller

import com.devlink.backend.dto.*
import com.devlink.backend.security.UserPrincipal
import com.devlink.backend.service.CertificateService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/certificates")
class CertificateController(
    private val certificateService: CertificateService
) {

    @PostMapping
    fun uploadCertificate(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UploadCertificateRequest
    ): ResponseEntity<CertificateResponse> {
        val certificate = certificateService.uploadCertificate(principal.id, request)
        return ResponseEntity.ok(certificate)
    }

    @GetMapping("/student/{studentId}")
    fun getStudentCertificates(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable studentId: Long
    ): ResponseEntity<List<CertificateResponse>> {
        val certificates = certificateService.getStudentCertificates(studentId, principal.id)
        return ResponseEntity.ok(certificates)
    }
}
