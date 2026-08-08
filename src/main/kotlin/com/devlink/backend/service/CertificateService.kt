package com.devlink.backend.service

import com.devlink.backend.dto.*

interface CertificateService {
    fun uploadCertificate(studentId: Long, request: UploadCertificateRequest): CertificateResponse
    fun getStudentCertificates(studentId: Long, currentUserId: Long): List<CertificateResponse>
}
