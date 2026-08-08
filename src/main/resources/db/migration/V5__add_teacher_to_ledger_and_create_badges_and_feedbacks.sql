-- V5__add_teacher_to_ledger_and_create_badges_and_feedbacks.sql

-- 1. Add teacher_id column to points_ledger referencing users(id)
ALTER TABLE points_ledger ADD COLUMN teacher_id BIGINT REFERENCES users (id) ON DELETE SET NULL;

-- 2. Create student_badges table
CREATE TABLE student_badges (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT NOT NULL REFERENCES student_profiles (user_id) ON DELETE CASCADE,
    badge_name  VARCHAR(100) NOT NULL,
    earned_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, badge_name)
);

-- 3. Create teacher_feedbacks table
CREATE TABLE teacher_feedbacks (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT NOT NULL REFERENCES student_profiles (user_id) ON DELETE CASCADE,
    teacher_id  BIGINT NOT NULL REFERENCES teacher_profiles (user_id) ON DELETE CASCADE,
    content     VARCHAR(2000) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
