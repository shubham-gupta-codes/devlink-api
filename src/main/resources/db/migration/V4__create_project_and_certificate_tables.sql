-- V4__create_project_and_certificate_tables.sql
-- Schema definition for projects, images, comments, likes, certificates, verification requests, and points ledger

CREATE TABLE projects (
    id                  BIGSERIAL PRIMARY KEY,
    student_id          BIGINT NOT NULL REFERENCES student_profiles (user_id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(2000) NOT NULL,
    tech_stack          VARCHAR(1000) NOT NULL, -- comma-separated list of tags
    github_url          VARCHAR(512),
    demo_url            VARCHAR(512),
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, NEED_TO_TALK
    like_count          BIGINT NOT NULL DEFAULT 0,
    comment_count       BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project_images (
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    image_url   VARCHAR(512) NOT NULL
);

CREATE TABLE project_likes (
    project_id  BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id)
);

CREATE TABLE project_comments (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    user_id             BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    parent_comment_id   BIGINT REFERENCES project_comments (id) ON DELETE CASCADE,
    content             VARCHAR(1000) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE certificates (
    id                  BIGSERIAL PRIMARY KEY,
    student_id          BIGINT NOT NULL REFERENCES student_profiles (user_id) ON DELETE CASCADE,
    title               VARCHAR(255) NOT NULL,
    issuing_org         VARCHAR(255) NOT NULL,
    purpose             VARCHAR(1000) NOT NULL,
    file_url            VARCHAR(512) NOT NULL,
    file_type           VARCHAR(50) NOT NULL, -- PDF, IMAGE
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' -- PENDING, APPROVED, REJECTED, NEED_TO_TALK
);

CREATE TABLE verification_requests (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(50) NOT NULL, -- SKILL, CERTIFICATE, PROJECT
    entity_id       BIGINT NOT NULL,
    requested_by    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, NEED_TO_TALK
    reviewed_by     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reviewed_at     TIMESTAMP,
    remarks         VARCHAR(1000),
    schedule_at     TIMESTAMP
);

CREATE TABLE points_ledger (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT NOT NULL REFERENCES student_profiles (user_id) ON DELETE CASCADE,
    point_source    VARCHAR(50) NOT NULL, -- VERIFIED_SKILL, VERIFIED_PROJECT, VERIFIED_CERTIFICATE
    points          INT NOT NULL,
    reference_id    BIGINT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
