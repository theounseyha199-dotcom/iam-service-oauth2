--liquibase formatted sql
--changeset codex:010-email-verification
-- Existing accounts retain their access without claiming their emails are verified.
-- New self-registrations enroll in this policy explicitly.
CREATE TABLE email_verification_required (
 user_uuid UUID PRIMARY KEY REFERENCES users(uuid)
);
CREATE TABLE email_verification_tokens (
 user_uuid UUID PRIMARY KEY REFERENCES users(uuid),
 token_hash VARCHAR(64) NOT NULL UNIQUE,
 email VARCHAR(255) NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL,
 requested_at TIMESTAMPTZ NOT NULL
);
