package com.devlink.backend.service.impl

import com.devlink.backend.dto.*
import com.devlink.backend.entity.*
import com.devlink.backend.enums.*
import com.devlink.backend.repository.*
import com.devlink.backend.service.CertificateService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CertificateServiceImpl(
    private val studentProfileRepository: StudentProfileRepository,
    private val teacherProfileRepository: TeacherProfileRepository,
    private val userRepository: UserRepository,
    private val certificateRepository: CertificateRepository,
    private val verificationRequestRepository: VerificationRequestRepository,
    private val notificationRepository: NotificationRepository
) : CertificateService {

    @Transactional
    override fun uploadCertificate(studentId: Long, request: UploadCertificateRequest): CertificateResponse {
        val student = studentProfileRepository.findById(studentId)
            .orElseThrow { IllegalArgumentException("Student profile not found") }

        // 1. Create certificate with status PENDING
        val certificate = Certificate(
            student = student,
            title = request.title,
            issuingOrg = request.issuingOrg,
            purpose = request.purpose,
            fileUrl = request.fileUrl,
            fileType = request.fileType,
            verificationStatus = VerificationStatus.PENDING
        )
        val savedCertificate = certificateRepository.save(certificate)

        // 2. Select teacher using round-robin sequence
        val studentBranch = student.branch ?: throw IllegalArgumentException("Student does not have a branch configured.")
        val teachers = teacherProfileRepository.findAll().filter {
            val tDept = it.department.uppercase()
            val sBranch = studentBranch.name.uppercase()
            tDept.contains(sBranch) ||
            (sBranch == "CS" && tDept.contains("COMPUTER SCIENCE")) ||
            (sBranch == "IT" && tDept.contains("INFORMATION TECHNOLOGY"))
        }
        if (teachers.isEmpty()) {
            throw IllegalArgumentException("No teachers registered in department matching ${studentBranch.name}")
        }
        val assignedTeacher = teachers.minByOrNull { teacher ->
            verificationRequestRepository.countByReviewedById(teacher.id!!)
        } ?: teachers.first()

        // 3. Create Verification Request
        val verificationRequest = VerificationRequest(
            entityType = EntityType.CERTIFICATE,
            entityId = savedCertificate.id!!,
            requestedBy = student.user,
            status = VerificationStatus.PENDING,
            reviewedBy = userRepository.findById(assignedTeacher.id!!).get()
        )
        verificationRequestRepository.save(verificationRequest)

        // 4. Send notification to the teacher
        val notification = Notification(
            user = userRepository.findById(assignedTeacher.id!!).get(),
            title = "New Certificate Verification Request",
            message = "@${student.user.username} requested verification for certificate: ${savedCertificate.title}"
        )
        notificationRepository.save(notification)

        return mapToResponse(savedCertificate)
    }

    override fun getStudentCertificates(studentId: Long, currentUserId: Long): List<CertificateResponse> {
        val certificates = if (studentId == currentUserId) {
            certificateRepository.findByStudentId(studentId)
        } else {
            certificateRepository.findByStudentIdAndVerificationStatus(studentId, VerificationStatus.APPROVED)
        }
        return certificates.map { mapToResponse(it) }
    }

    private fun mapToResponse(certificate: Certificate): CertificateResponse {
        val req = verificationRequestRepository.findByEntityTypeAndEntityId(EntityType.CERTIFICATE, certificate.id!!)
        return CertificateResponse(
            id = certificate.id!!,
            studentUserId = certificate.student.id!!,
            studentUsername = certificate.student.user.username,
            studentName = certificate.student.name,
            title = certificate.title,
            issuingOrg = certificate.issuingOrg,
            purpose = certificate.purpose,
            fileUrl = certificate.fileUrl,
            fileType = certificate.fileType,
            verificationStatus = certificate.verificationStatus,
            teacherRemarks = req?.remarks
        )
    }
}
