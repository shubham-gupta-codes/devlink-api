package com.devlink.backend.enums

enum class Course {
    BSC_IT,
    BTECH,
    MTECH,
    MCA,
    BCA,
    MSC
}

enum class Branch {
    IT,
    CS,
    CYBER_SECURITY,
    AI,
    DATA_SCIENCE
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
