-- V2__create_profile_tables.sql
-- Table definitions for student_profiles, teacher_profiles, recruiter_profiles, social_links

CREATE TABLE student_profiles (
    user_id         BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    photo_url       VARCHAR(512),
    course          VARCHAR(50),
    branch          VARCHAR(50),
    admission_year  INT,
    semester        INT,
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    bio             VARCHAR(1000),
    total_points    INT NOT NULL DEFAULT 0,
    contact         VARCHAR(50)
);

CREATE TABLE teacher_profiles (
    user_id         BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    employee_id     BIGINT,
    department      VARCHAR(255) NOT NULL,
    designation     VARCHAR(255)
);

CREATE TABLE recruiter_profiles (
    user_id         BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    company_name    VARCHAR(255) NOT NULL,
    company_logo    VARCHAR(512),
    designation     VARCHAR(255),
    contact         VARCHAR(50)
);

CREATE TABLE social_links (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    platform        VARCHAR(50) NOT NULL,
    url             VARCHAR(512) NOT NULL,
    CONSTRAINT uq_social_links_user_platform UNIQUE (user_id, platform)
);

CREATE INDEX idx_social_links_user ON social_links (user_id);
