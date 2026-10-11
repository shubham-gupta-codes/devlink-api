-- V7__create_jobs_and_applications.sql

-- Add new company metadata fields to recruiter_profiles
ALTER TABLE recruiter_profiles ADD COLUMN about VARCHAR(1000);
ALTER TABLE recruiter_profiles ADD COLUMN website VARCHAR(255);
ALTER TABLE recruiter_profiles ADD COLUMN verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Job Postings table
CREATE TABLE job_postings (
    id              BIGSERIAL PRIMARY KEY,
    recruiter_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    description     TEXT NOT NULL,
    job_type        VARCHAR(50) NOT NULL, -- INTERNSHIP, PART_TIME, FULL_TIME, CONTRACT
    required_skills VARCHAR(1000) NOT NULL, -- Comma-separated list of required skill names
    location        VARCHAR(255) NOT NULL,
    salary_range    VARCHAR(100),
    deadline        TIMESTAMP NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'OPEN', -- OPEN, CLOSED
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Job Applications table
CREATE TABLE job_applications (
    id              BIGSERIAL PRIMARY KEY,
    job_id          BIGINT NOT NULL REFERENCES job_postings (id) ON DELETE CASCADE,
    student_id      BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, SHORTLISTED, REJECTED
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_job_applications_job_student UNIQUE (job_id, student_id)
);

-- Saved Candidates table
CREATE TABLE saved_candidates (
    id              BIGSERIAL PRIMARY KEY,
    recruiter_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    student_id      BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    job_id          BIGINT REFERENCES job_postings (id) ON DELETE SET NULL, -- Optional tag to specific job
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_saved_candidates UNIQUE (recruiter_id, student_id, job_id)
);
