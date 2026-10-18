-- V10__create_stored_files_table.sql
-- Store uploaded files (profile photos, project screenshots, certificates) persistently in Postgres
-- Prevents file loss on ephemeral cloud containers (e.g. Render/Railway redeployments)

CREATE TABLE IF NOT EXISTS stored_files (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    data BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_stored_files_filename UNIQUE (filename)
);

CREATE INDEX IF NOT EXISTS idx_stored_files_filename ON stored_files (filename);
