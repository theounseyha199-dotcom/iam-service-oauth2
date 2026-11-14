--liquibase formatted sql
--changeset codex:012-management-review
ALTER TABLE iam_deactivation_request
 ADD COLUMN status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
 ADD COLUMN reviewed_by UUID,
 ADD COLUMN reviewed_at TIMESTAMPTZ,
 ADD COLUMN review_note VARCHAR(500),
 ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
 ADD CONSTRAINT iam_deactivation_status CHECK (status IN ('PENDING','APPROVED','REJECTED'));
CREATE INDEX iam_deactivation_status_time ON iam_deactivation_request(status, requested_at DESC);
ALTER TABLE iam_activity ADD COLUMN target VARCHAR(200);
