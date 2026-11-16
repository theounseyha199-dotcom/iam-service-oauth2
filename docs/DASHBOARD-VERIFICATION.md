# Assignment IV dashboard verification

Scope: `IV. Assignment - IAM Service _ Dashboard.docx.pdf`, Account (40 points)
and Management (50 points). Video delivery is excluded at the owner's request.
Implementation coverage does not establish a grade or prove external provider acceptance.

## Objective

Verify the React `iam-web` interface integrated with the IAM REST API, secure
authentication/session management, responsive layouts, and English/Khmer. Keep
PostgreSQL for application persistence. Follow the supplied workflow in the exact
order below. A passing automated flow does not certify live third-party consent.

## Account (+40 points)

| Step | Assignment requirement | Implementation and evidence |
| --- | --- | --- |
| A1 (+5) | Profile | React profile view/edit; name, username, email, phone and picture URL; duplicate/stale-edit tests |
| A2 (+5) | Security & Password | Change/recovery/reset forms; CSRF, expiring single-use links, configured password rules; reset after renaming regression |
| A3 (+5) | Linked Account | Provider/subject ownership, explicit link/unlink, cancellation and last-method protection; live-provider acceptance pending |
| A4 (+5) | Active Sessions | Device/browser, IP, last activity and offline location lookup; real public-IP locations pending a configured dataset |
| A5 (+5) | Session Management | Own individual/all-session termination; ownership and expired-session HTTP tests |
| A6 (+5) | Login History | Success/failure, timestamps, device/IP, filtering and pagination |
| A7 (+5) | Account Activity | Profile/password/security events and outcomes |
| A8 (+5) | Account Deactivation | Persisted request, status and administrative review |

## Management (+50 points)

| Step | Assignment requirement | Implementation and evidence |
| --- | --- | --- |
| M1 (+2.5) | Admin Overview | Permission-filtered user/active-user/role/permission/client totals and system recent activity |
| M2 (+2.5) | User | Create/read/update/soft-delete/enable/disable/lock/unlock |
| M3 (+2.5) | User Role Assignment | Assign/revoke; changing roles uses those operations |
| M4 (+2.5) | User Permission Assignment | GRANT/DENY/revoke and effective-authority display |
| M5 (+5) | User Sessions | Administrator inspection and individual/all-session termination |
| M6 (+5) | User Login History | Per-user history, failures and repeated-failure indicator |
| M7 (+5) | Role | CRUD and assign/remove permissions |
| M8 (+5) | Permission | CRUD with resource/action-based permission names |
| M9 (+5) | Client | CRUD, masked credential entry, grant types, redirects, scopes and lifetimes |
| M10 (+5) | Account Security Management | Administrative password reset, unlock and status controls |
| M11 (+5) | Audit Logs | Actor, target, event, outcome, date, repeated-failure and pagination filters |
| M12 (+5) | System Configuration | Enforced password rules, session idle timeout, consent and token defaults; existing client lifetimes are edited separately |

## Confirmed defects and regression checks

Three HTTP integration tests failed against the original implementation:

- Wrong/missing JWT audiences were accepted by `/api/me`.
- Renaming a user hid their existing connected applications.
- Password reset after renaming left the original browser session/refresh grant usable.

The fixes use the immutable UUID as Spring's authenticated principal name and OAuth
grant/consent owner. Login still accepts the username/email and profile displays the
editable username. Password reset expires sessions by UUID and removes the UUID's
stored authorizations. The API requires `iam-api` (or `IAM_API_AUDIENCE`) in `aud`.
Keep the audience issuance rules aligned if changing that setting.

Migration `013-stable-oauth-subjects.sql` maps existing user grants using the saved
principal UUID, including renamed owners, and preserves their consents. It leaves
client-credentials subjects unchanged. Legacy consents without a provable owner
are cleared so the user must approve consent again. A dedicated integration test replays legacy
username-owned grants and verifies connected-app access and refresh after migration.
Previously issued JWT access tokens retain their existing expiry behavior.

## Verification command

Latest completed verification: **70 JUnit tests, zero failures/errors/skips**,
including both account/management browser suites and the packaged-JAR check.
ESLint, schema/English-Khmer resource checks, device/router checks and strict static
UI audit passed. Machine-readable evidence: `verification/dashboard-assignment-check.json`.
This verifies automated coverage; A3 live-provider acceptance and A4 real public-IP
location setup remain pending as described below.

```sh
./gradlew check :iam-main:bootJar -Ppart2Integration=true -PbrowserChecks=true -PpackagedChecks=true
node scripts/account-unit-check.mjs
```

The Gradle command includes isolated PostgreSQL/Redis integration tests, both real
Chromium browser suites (account and management), English/Khmer and mobile flows,
frontend lint/schema/translation checks and the packaged-JAR smoke test. Current
JUnit results are under `iam-main/build/test-results/test`; browser process logs and
screenshots are under `verification`. Static UI audit is supplementary evidence.

## Configuration and acceptance still needed

- **GeoIP:** no real database is configured in the local environment. Set
  `IAM_GEOIP_DATABASE` to an absolute path to a real GeoLite2/GeoIP2 City `.mmdb`
  file and restart. Never use the synthetic test fixture as real geolocation data.
  Public-IP fixture lookup and local/unavailable fallbacks are covered by tests.
- **Live social providers:** local credential pairs exist for Google, GitHub,
  Facebook and Telegram. Synthetic integration tests cover starts/state/PKCE,
  identity mapping, ownership, cancellation and unlink rules. Actual provider
  account consent/MFA and dashboard-approved callbacks require a live acceptance
  run; configured credentials alone do not prove these work.
- **Session scope:** session enumeration/revocation covers this IAM process.
  Deployments with multiple instances require shared session storage.
- **Pictures:** profile picture editing accepts a URL; an upload service is not
  expressly required by the assignment.

Do not mark the GeoIP setup or live-provider acceptance complete without the
corresponding real configuration and runtime evidence.

Live Google attempt: the configured client reached `accounts.google.com`, but
Google rejected the automated browser with “This browser or app may not be secure.”
The password step was not reached. IAM is available at `http://localhost:9000/login`
for a regular-browser consent run. This does not verify callback completion,
linking or unlinking; no automated-browser restriction was bypassed.

## Owner database configuration

The local CLI and private IntelliJ run configuration now target
`jdbc:postgresql://localhost:9543/db_oauth2`, using the tested additive
`changelog-legacy.yaml` compatibility path. This preserves the original Liquibase
history and core data rather than replaying the fresh-database CREATE statements.
The configured administrator is `theounseyha`; its enabled ADMIN assignment was
verified in that database. Restart existing IntelliJ processes to load the new
environment. Missing/deleted browser-session users now redirect to login, and
workspace API requests return 401 instead of producing an uncaught not-found error.
