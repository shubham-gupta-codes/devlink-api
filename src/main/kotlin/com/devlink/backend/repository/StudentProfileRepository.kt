package com.devlink.backend.repository

import com.devlink.backend.entity.StudentProfile
import com.devlink.backend.entity.User
import com.devlink.backend.enums.Course
import com.devlink.backend.enums.Branch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface StudentProfileRepository : JpaRepository<StudentProfile, Long> {
    fun findByUser(user: User): Optional<StudentProfile>
    fun findByUserUsername(username: String): Optional<StudentProfile>

    @Query("SELECT DISTINCT s FROM StudentProfile s " +
           "LEFT JOIN s.user u " +
           "LEFT JOIN StudentSkill ss ON ss.student = s " +
           "LEFT JOIN StudentBadge sb ON sb.student = s " +
           "WHERE (:query IS NULL OR :query = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:course IS NULL OR s.course = :course) " +
           "AND (:branch IS NULL OR s.branch = :branch) " +
           "AND (:admissionYear IS NULL OR s.admissionYear = :admissionYear) " +
           "AND (:semester IS NULL OR s.semester = :semester) " +
           "AND (:skill IS NULL OR :skill = '' OR LOWER(ss.skill.name) = LOWER(:skill)) " +
           "AND (:badge IS NULL OR :badge = '' OR LOWER(sb.badgeName) = LOWER(:badge)) " +
           "ORDER BY s.totalPoints DESC")
    fun searchStudents(
        @Param("query") query: String?,
        @Param("course") course: Course?,
        @Param("branch") branch: Branch?,
        @Param("admissionYear") admissionYear: Int?,
        @Param("semester") semester: Int?,
        @Param("skill") skill: String?,
        @Param("badge") badge: String?
    ): List<StudentProfile>
}
