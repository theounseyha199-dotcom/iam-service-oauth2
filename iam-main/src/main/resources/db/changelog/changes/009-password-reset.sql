--liquibase formatted sql
--changeset codex:009-password-reset
CREATE TABLE password_reset_tokens (
 user_uuid UUID PRIMARY KEY REFERENCES users(uuid),
 token_hash VARCHAR(64) NOT NULL UNIQUE,
 email VARCHAR(255) NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL,
 requested_at TIMESTAMPTZ NOT NULL
);
