-- V1__create_auth_tables.sql
-- Auth module: users, refresh_tokens, email_otps
-- Matches entity/User.kt, entity/RefreshToken.kt, entity/EmailOtp.kt
-- and the users / refresh_tokens sections of Database_Schema.docx.

-- Plain VARCHAR + CHECK, not a native Postgres ENUM type: Hibernate's @Enumerated(EnumType.STRING)
-- maps to VARCHAR by default, and ddl-auto=validate would fail against a custom enum type.
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL
                    CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN', 'RECRUITER')),
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL,
    expired_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_refresh_tokens_token UNIQUE (token)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE email_otps (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    otp_code    VARCHAR(10)  NOT NULL,
    expired_at  TIMESTAMP    NOT NULL,
    attempts    INT          NOT NULL DEFAULT 0,
    verified    BOOLEAN      NOT NULL DEFAULT FALSE
);

-- verifyOtp() always looks up the most recent OTP for an email — this index makes that fast.
CREATE INDEX idx_email_otps_email_id ON email_otps (email, id DESC);
