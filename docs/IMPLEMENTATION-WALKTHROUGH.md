# Explain the implementation step by step

## Step 1: Keep the module boundaries

`settings.gradle` includes exactly the Section A modules. The domain owns rules;
the application owns actions and ports. REST, web, persistence and authorization
are adapters. Main composes them through explicit Spring bean configuration. The files under
`co/istad/iam/config` group user, role, permission and identity use cases;
`ApplicationConfiguration` owns the single clock bean and shared support services.

## Step 2: Model relationships as assignments

Users, roles and permissions are connected through `UserRole`, `RolePermission`
and `UserPermission`. Each assignment stores its actor/time, expiry, revocation and
reason. Its own JPA entity stores a historical row. Revocation changes metadata;
it never removes the row. The authority calculator checks the assignment's time
window and target flags before applying GRANT minus DENY.

## Step 3: Execute a single application action

For example, `CreateRoleUseCase` accepts a `CreateRoleCommand`, creates a validated
Role, checks uniqueness, saves it through the role port, and invokes assignment
logic for its initial permission UUIDs. A transaction covers the entire operation.
The application contains no controller, HTTP, JPA, or authorization-server types.

## Step 4: Persist through a port adapter

`RolePersistenceAdapter` maps the domain model to `RoleEntity`, uses Spring Data,
and maps the saved row back, including its updated version. Owner locks serialize
assignment writes; optimistic versions catch stale entity updates. Liquibase creates
the schema and seeds; Hibernate only validates it.

## Step 5: Expose REST actions

A Section D controller requires the exact permission, validates a request DTO and
calls one use case. Explicit response projections omit credentials/provider snapshots.
List responses contain `content` and `page`. Error advice returns Problem Details;
security failures also return JSON instead of redirecting API clients to login.

## Step 6: Issue and validate tokens

The Authorization Server loads internal users through the authentication use case.
The principal keeps the user's UUID and account flags; password credentials are
erased. JDBC persists registered clients, grants and consents. A persistent RSA key
signs JWTs. Token customization reloads current account status/authorities, sets the
UUID subject and adds only OIDC claims allowed by the authorized scopes.

The REST chain verifies that the token is signed by IAM, has the configured issuer,
has an expiration and is an access token. Permission claims become authorities.
An OIDC scope or external provider role does not grant administrative API access.

## Step 7: Register clients dynamically

The registration client uses client credentials and `client.create` to obtain an
initial token. POST `/connect/register` creates a consented, PKCE-enabled OIDC client.
Its registration token permits reading that client; GET never exposes its stored
secret hash. The policy rejects privileged scopes and unsupported grants/metadata.

## Step 8: Translate a trusted provider identity

Spring validates OAuth state and OIDC token proof where applicable. The provider
mapper extracts the signed subject or authenticated provider ID and a limited
profile snapshot. The application then finds or provisions the internal user.
Matching email never merges identities. Missing required fields use the short-lived
completion form; linking is bound to the signed-in owner and original server state.

## Step 9: Render browser pages

JTE controllers call the same use cases as REST. Shared layout/field/notice/CSRF
partials keep forms consistent. Message bundles own visible English/Khmer text.
Save actions redirect with success; invalid input preserves safe fields; secrets
are cleared. Unlink and connected-app revocation require confirmation and CSRF.
Dirty edits use an accessible dialog before in-app navigation.

## Step 10: Prove the result

The full test command creates disposable PostgreSQL clusters, applies migrations,
checks the complete REST workflow and OAuth lifecycle, opens Chromium for account
flows and mobile/locale checks, then starts the packaged JAR from outside the source
tree to prove templates are bundled. Static audits check module boundaries,
required classes/routes/pages, message-key parity, and empty Postman secrets.
