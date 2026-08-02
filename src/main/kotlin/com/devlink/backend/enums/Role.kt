package com.devlink.backend.enums

/**
 * Matches `role` enum on the `users` table in Database_Schema.docx.
 * Also doubles as the Spring Security authority (prefixed with ROLE_ at grant time).
 */
enum class Role {
    STUDENT,
    TEACHER,
    ADMIN,
    RECRUITER
}
