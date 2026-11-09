--liquibase formatted sql

--changeset seyha:004-add-role-permission-revocation-reason
ALTER TABLE role_permissions
    ADD COLUMN revocation_reason VARCHAR(255);

--rollback ALTER TABLE role_permissions DROP COLUMN revocation_reason;
