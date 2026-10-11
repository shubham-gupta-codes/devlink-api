package com.devlink.backend.enums

enum class Course {
    BSC_IT,
    BSC,
    BTECH,
    MTECH,
    MCA,
    BCA,
    MSC,
    OTHER
}

enum class Branch {
    IT,
    CS,
    ECE,
    EE,
    ME,
    CE,
    CYBER_SECURITY,
    AI,
    DATA_SCIENCE,
    OTHER
}

enum class AcademicStatus {
    ACTIVE,
    GRADUATED
}

enum class SocialPlatform {
    GITHUB,
    LINKEDIN,
    PORTFOLIO,
    LEETCODE,
    CODEFORCES,
    HACKERRANK,
    KAGGLE,
    YOUTUBE,
    INSTAGRAM,
    TWITTER,
    MEDIUM,
    DEVTO,
    BEHANCE,
    DRIBBBLE
}

enum class SkillStatus {
    PENDING,
    MEET,
    ACCEPTED,
    REJECTED
}

enum class SkillLevel {
    LOW,
    MID,
    HIGH
}

enum class VerificationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    NEED_TO_TALK
}

enum class EntityType {
    SKILL,
    PROJECT,
    CERTIFICATE
}

enum class FileType {
    PDF,
    IMAGE
}

enum class PointSource {
    VERIFIED_SKILL,
    VERIFIED_PROJECT,
    VERIFIED_CERTIFICATE
}

enum class JobType {
    INTERNSHIP,
    PART_TIME,
    FULL_TIME,
    CONTRACT
}

enum class JobStatus {
    OPEN,
    CLOSED
}

enum class JobApplicationStatus {
    PENDING,
    SHORTLISTED,
    REJECTED
}
