--liquibase formatted sql
--changeset codex:013-stable-oauth-subjects
-- The saved authentication contains the actual owner, even if their username was renamed/reused.
-- A consent without one provable owner must be approved again, never assigned to
-- whoever happens to own the old username now.
DELETE FROM oauth2_authorization_consent c
WHERE NOT EXISTS (
 SELECT 1 FROM oauth2_authorization a
 JOIN users u ON u.uuid::text = a.attributes::jsonb #>> '{java.security.Principal,principal,uuid}'
 WHERE a.registered_client_id = c.registered_client_id AND a.principal_name = c.principal_name
 AND a.authorization_grant_type <> 'client_credentials'
 GROUP BY a.registered_client_id, a.principal_name
 HAVING count(DISTINCT u.uuid) = 1
);

-- Move consent before changing the matching authorization's old principal name.
WITH owners AS (
 SELECT a.registered_client_id, a.principal_name,
        min(a.attributes::jsonb #>> '{java.security.Principal,principal,uuid}') AS subject
 FROM oauth2_authorization a
 WHERE a.authorization_grant_type <> 'client_credentials'
 GROUP BY a.registered_client_id, a.principal_name
 HAVING count(DISTINCT a.attributes::jsonb #>> '{java.security.Principal,principal,uuid}') = 1
)
UPDATE oauth2_authorization_consent c SET principal_name = owners.subject
FROM owners JOIN users u ON u.uuid::text = owners.subject
WHERE c.registered_client_id = owners.registered_client_id AND c.principal_name = owners.principal_name;

UPDATE oauth2_authorization a SET principal_name = u.uuid::text
FROM users u
WHERE a.attributes::jsonb #>> '{java.security.Principal,principal,uuid}' = u.uuid::text
AND a.authorization_grant_type <> 'client_credentials';
