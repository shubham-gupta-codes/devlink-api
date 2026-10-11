-- V9__add_start_date_to_job_postings.sql
ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS start_date TIMESTAMP;
