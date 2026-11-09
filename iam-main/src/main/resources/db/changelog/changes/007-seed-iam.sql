--liquibase formatted sql

--changeset codex:007-seed-iam-roles-and-permissions
INSERT INTO roles (uuid, name, description, is_system, is_active, created_at, created_by, updated_at, updated_by)
VALUES
('00000000-0000-0000-0000-000000000001', 'ADMIN', 'System administrator', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration'),
('00000000-0000-0000-0000-000000000002', 'USER', 'Default registered user', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('89434513-93d8-50ab-b4a9-f44949273941', 'user:create', 'user', 'create', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('b10b6ab3-6616-5db7-9657-2d5a0c4993c0', 'user:read', 'user', 'read', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('b3370788-4a8f-5e30-a8df-fb7da6930d05', 'user:update', 'user', 'update', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('bb17c78f-95e2-5ce1-9778-089b35159561', 'user:delete', 'user', 'delete', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('57a1711a-e0f1-5f7b-8ea7-6252eddec27a', 'user:assign', 'user', 'assign', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('c7fb7c7c-0201-5790-a970-e74950de31e0', 'role:create', 'role', 'create', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('17727751-19d2-5dee-8690-026f9377fcea', 'role:read', 'role', 'read', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('57543174-6b04-540f-a236-266ae97f61a6', 'role:update', 'role', 'update', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('60c9961b-7ee5-543a-899e-d757656ce59c', 'role:delete', 'role', 'delete', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('f496b69b-1f02-578f-961a-9db60fced77f', 'role:assign', 'role', 'assign', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('340c0bbd-3a38-57d2-9714-d500a9125ecf', 'permission:create', 'permission', 'create', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('f5416efd-74e3-5532-aff1-330530bee379', 'permission:read', 'permission', 'read', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('ab51d702-b1ec-5cdf-9917-cbc1c226ffe8', 'permission:update', 'permission', 'update', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO permissions (uuid, name, resource, action, is_system, is_active, created_at, created_by, updated_at, updated_by) VALUES ('13ef1bf7-4e85-5ed1-8fbb-5bf79e3d47d1', 'permission:delete', 'permission', 'delete', TRUE, TRUE, CURRENT_TIMESTAMP, 'migration', CURRENT_TIMESTAMP, 'migration');
INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by, reason)
SELECT r.id, p.id, CURRENT_TIMESTAMP, 'migration', 'Initial administrator permissions'
FROM roles r CROSS JOIN permissions p WHERE r.name = 'ADMIN';

-- USER starts with no administrative API permissions; /api/me is authenticated-only in Section D.
