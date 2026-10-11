-- V8__add_updated_at_to_teacher_feedbacks.sql
ALTER TABLE teacher_feedbacks ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
