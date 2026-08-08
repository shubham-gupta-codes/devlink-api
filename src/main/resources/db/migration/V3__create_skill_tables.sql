-- V3__create_skill_tables.sql
-- Table definitions for skills, student_skills, skill_requests, notifications

CREATE TABLE skills (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE student_skills (
    id                  BIGSERIAL PRIMARY KEY,
    student_id          BIGINT NOT NULL REFERENCES student_profiles (user_id) ON DELETE CASCADE,
    skill_id            BIGINT NOT NULL REFERENCES skills (id) ON DELETE CASCADE,
    level               VARCHAR(50) NOT NULL, -- LOW, MID, HIGH
    points_awarded      INT NOT NULL,
    approved_by         BIGINT REFERENCES teacher_profiles (user_id) ON DELETE SET NULL,
    approved_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_skills_student_skill UNIQUE (student_id, skill_id)
);

CREATE TABLE skill_requests (
    id                  BIGSERIAL PRIMARY KEY,
    student_id          BIGINT NOT NULL REFERENCES student_profiles (user_id) ON DELETE CASCADE,
    skill_id            BIGINT NOT NULL REFERENCES skills (id) ON DELETE CASCADE,
    teacher_id          BIGINT NOT NULL REFERENCES teacher_profiles (user_id) ON DELETE CASCADE,
    status              VARCHAR(50) NOT NULL, -- PENDING, MEET, ACCEPTED, REJECTED
    meeting_date        TIMESTAMP,
    meeting_notes       VARCHAR(1000),
    rejection_reason    VARCHAR(1000),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    message     VARCHAR(1000) NOT NULL,
    read        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed default skills catalog
INSERT INTO skills (name) VALUES 
('Kotlin'),
('Java'),
('Spring Boot'),
('Python'),
('SQL'),
('React'),
('JavaScript'),
('HTML/CSS'),
('Android Development')
ON CONFLICT (name) DO NOTHING;
