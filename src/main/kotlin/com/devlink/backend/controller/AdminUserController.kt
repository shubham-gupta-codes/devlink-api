package com.devlink.backend.controller

import com.devlink.backend.dto.*
import com.devlink.backend.enums.Role
import com.devlink.backend.service.AdminService
import com.devlink.backend.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(
    private val userService: UserService,
    private val adminService: AdminService
) {

    /** Creates a user account and emails credentials. */
    @PostMapping("/user")
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val created = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    /** GET /api/admin/stats — platform-wide metrics. */
    @GetMapping("/stats")
    fun getDashboardStats(): ResponseEntity<AdminDashboardStats> {
        return ResponseEntity.ok(adminService.getDashboardStats())
    }

    /** GET /api/admin/users — list and filter accounts. */
    @GetMapping("/users")
    fun listUsers(
        @RequestParam(required = false) role: Role?,
        @RequestParam(required = false) enabled: Boolean?
    ): ResponseEntity<List<AdminUserListItem>> {
        return ResponseEntity.ok(adminService.listAllUsers(role, enabled))
    }

    /** PATCH /api/admin/users/{id}/suspend */
    @PatchMapping("/users/{id}/suspend")
    fun suspendUser(@PathVariable id: Long): ResponseEntity<Unit> {
        adminService.suspendUser(id)
        return ResponseEntity.ok().build()
    }

    /** PATCH /api/admin/users/{id}/reactivate */
    @PatchMapping("/users/{id}/reactivate")
    fun reactivateUser(@PathVariable id: Long): ResponseEntity<Unit> {
        adminService.reactivateUser(id)
        return ResponseEntity.ok().build()
    }

    /** DELETE /api/admin/users/{id} */
    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Unit> {
        adminService.deleteUser(id)
        return ResponseEntity.ok().build()
    }

    /** GET /api/admin/approvals/recruiters */
    @GetMapping("/approvals/recruiters")
    fun getPendingRecruiters(): ResponseEntity<List<PendingRecruiterResponse>> {
        return ResponseEntity.ok(adminService.getPendingRecruiters())
    }

    /** PATCH /api/admin/approvals/recruiters/{id}/approve */
    @PatchMapping("/approvals/recruiters/{id}/approve")
    fun approveRecruiter(@PathVariable id: Long): ResponseEntity<Unit> {
        adminService.approveRecruiter(id)
        return ResponseEntity.ok().build()
    }

    /** PATCH /api/admin/approvals/recruiters/{id}/reject */
    @PatchMapping("/approvals/recruiters/{id}/reject")
    fun rejectRecruiter(
        @PathVariable id: Long,
        @RequestBody request: RejectUserRequest
    ): ResponseEntity<Unit> {
        adminService.rejectRecruiter(id, request.reason)
        return ResponseEntity.ok().build()
    }
}
