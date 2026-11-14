# ISTAD IAM service

Implementation of **III. Assignment - Spring Authorization Server & IAM Service.pdf**,
Sections A–F: core domain, application use cases, JPA persistence, REST API, Spring
Authorization Server, social-login adapters, and English/Khmer React pages.

## 1. Start from a fresh clone

Requirements: **JDK 25**, Docker with Compose v2, and internet access for the first
Gradle dependency download. IntelliJ IDEA and Postman Desktop are optional tools.
No `.env` file, local reference repository, external Docker network, or existing
database is required. Run the commands below from the cloned repository root.

### Start PostgreSQL

Choose your own local passwords. In Bash, enter these commands; the prompts do not
print passwords on the terminal:

```bash
read -rsp "Choose a database password: " DB_PASSWORD; echo
export DB_PASSWORD
read -rsp "Choose an OIDC client secret: " IAM_OIDC_CLIENT_SECRET; echo
export IAM_OIDC_CLIENT_SECRET
read -rsp "Choose a registration client secret: " IAM_DCR_CLIENT_SECRET; echo
export IAM_DCR_CLIENT_SECRET
read -rsp "Choose an administrator password: " IAM_ADMIN_PASSWORD; echo
export IAM_ADMIN_PASSWORD
export DB_URL=jdbc:postgresql://localhost:9544/db_iam_service
export DB_USERNAME=iteuser
export IAM_ISSUER=http://localhost:9000

docker compose up -d --wait
docker compose ps
```

Use a strong administrator password (at least 12 characters with upper/lowercase,
a digit, and a symbol). The root `compose.yaml` creates this project's database and
volume automatically. It uses host port **9544** to avoid the original development
PostgreSQL server on 9543. It does not use `deployment/postgres`, which is the older
multi-service database setup. To change the review database port, export
`IAM_DB_PORT` before starting Compose and update `DB_URL` to match.

### Start the application

From the **same terminal**, with Java 25 selected:

```bash
java -version
./gradlew :iam-main:bootRun
```

Wait for `Started IamServiceApplication`, then open
[http://localhost:9000/login](http://localhost:9000/login). Liquibase creates and
seeds the tables; Hibernate validates them. Sign in with username **admin** and the
administrator password you chose. Startup creates the administrator only if the
username/email are unused. Changing `IAM_ADMIN_PASSWORD` does not reset an existing
account. Normal public registration assigns USER, not administrative permissions.

To run in IntelliJ instead, open the clone, sync Gradle, set the project SDK and
Gradle JVM to Java 25, then run the main method in
`iam-main/src/main/java/co/istad/iam/IamServiceApplication.java`. In **Run → Edit
Configurations → Environment variables**, set the same six required values:
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `IAM_OIDC_CLIENT_SECRET`,
`IAM_DCR_CLIENT_SECRET`, and `IAM_ADMIN_PASSWORD`. Keep the working directory at
the project root. Terminal exports are not automatically available to an IntelliJ
instance that was already running. Keep any configuration containing secrets local.
The author's private run configurations are not supplied by Git.

Stop the application with Ctrl+C (or IntelliJ Stop). Stop the review database with
`docker compose stop`. Restart it with `docker compose up -d --wait`, supplying the
same `DB_PASSWORD`. Existing database passwords are not changed by Compose variables.
Do not delete the volume to fix a password or migration error.

### Test with Postman

Import the **portable** collection and environment in [postman](postman/README.md).
Fill `oidc_client_secret` with the OIDC secret you chose. Select the collection's
**Authorization → Get New Access Token**, keep **Authorize using browser** unchecked,
sign in as `admin`, approve consent, and click **Use Token**. Requests inherit this
token. Send `GET http://localhost:9000/api/me`, then `GET http://localhost:9000/api/users`.
Both should return 200 for this seeded administrator. A normal USER should receive
403 on the administrative endpoint. A missing/expired token should receive 401.

Social logins are optional and need the reviewer's own provider credentials and
approved callbacks; core local authentication and API testing work without them.

For a guided code review and expected API results, read the
[reviewer guide](docs/REVIEWER-GUIDE.md).

## 2. Assignment walkthrough

| Part | Implementation |
| --- | --- |
| 1 — A/B | Seven modules, plain Java domain models, audit/deletion metadata, three assignment models and effective-authority rules |
| 2 — C | All 39 required use cases, commands/results/ports, seven JPA entities, transactional adapters, eight Liquibase changesets and ADMIN/USER seeds |
| 3 — D | All 39 specified REST routes, request validation, explicit safe response DTOs, pagination metadata, own-issuer JWT validation, exact permission authorities and RFC 9457 errors |
| 4 — E | JDBC registered clients/authorizations/consents, persistent RSA signing key, UUID subjects, scoped OIDC claims, current roles/permissions in access tokens, DCR and four social-provider adapters |
| 5 — F | All eight React pages, English/Khmer message bundles, language cookie, CSRF-protected forms, consent, linked identities and connected applications |

Read [the requirement checklist](docs/ASSIGNMENT-CHECKLIST.md) for exact files and
[the implementation walkthrough](docs/IMPLEMENTATION-WALKTHROUGH.md) to explain the code.

## 3. Modules and request flow

```text
iam-domain/iam-domain-core          Models and domain rules
iam-domain/iam-application-service  Use cases, commands, results, output ports
iam-persistence                    JPA entities, Spring Data repositories, adapters
iam-infrastructure                 Security, Authorization Server, federation, JDBC adapters
iam-rest-api                       Controllers, HTTP DTOs, validation, API security
iam-web                            React views and Spring MVC controllers, localization, shared browser controls
iam-main                           Entry point, bean wiring, configuration, migrations
```

A controller checks permissions and maps a DTO into a command. A use case applies
domain rules and calls repository ports inside a transaction. Persistence adapters
map domain objects to JPA entities. Domain/application modules import no Spring,
JPA, web, or Authorization Server classes. Every assignment has its own entity;
there is no `@ManyToMany`.

## 4. API, OAuth and Postman

### Swagger UI

Open [http://localhost:9000/swagger-ui/index.html](http://localhost:9000/swagger-ui/index.html).
Click **Authorize**, select the `iamOAuth` flow with `openid`, `profile`, and `email`,
then sign in and approve consent. The preconfigured `iam-swagger` client uses PKCE;
**no client secret is needed**. Use an administrator account for administrative APIs.
Expand an operation, click **Try it out**, fill its fields and click **Execute**.
Alternatively, paste an existing access token into the `bearerToken` authorization.
Scalar API Reference is available at [http://localhost:9000/scalar](http://localhost:9000/scalar).
It uses the same OpenAPI specification as Swagger UI. To test authenticated requests,
select the `bearerToken` security option and paste your JWT access token. API permissions
still apply; opening the documentation does not grant API access. Scalar's JavaScript
is served locally at `/scalar/scalar.js`.

Collections are organized as Authorization, Registration, My Account, Users, Roles,
and Permissions. Endpoint titles use action names such as Get my profile and List
users. Authorization documents the browser sign-in flow, token exchange, revocation,
introspection, and public signing keys; these endpoints are owned by Spring Security.

The OpenAPI JSON is at `/v3/api-docs`. Public registration needs no token.
Authorization is not persisted across page reloads. When using a different host or
port, set `IAM_ISSUER` to that application's external URL before startup so the
registered Swagger callback matches. Restart after adding this feature to seed
its dedicated public client. API authentication and permission checks still apply.


Import the collection and empty-secret environment under [postman](postman/README.md).
There are 51 requests including REST routes and OAuth/OIDC/DCR helpers.

- REST base: `/api`; registration is public; other routes require an IAM access token.
- Administrative routes check the exact `resource:action` permission in Section D.
  OAuth scopes or a role name alone are insufficient.
- `/api/me` derives identity from the validated JWT subject, never a request UUID.
- Authorization/token endpoints: `/oauth2/authorize`, `/oauth2/token`.
- OIDC metadata: `/.well-known/openid-configuration`; UserInfo: `/userinfo`.
- Seeded OIDC client: `iam-web-client`, authorization code + refresh token, PKCE S256,
  Basic client authentication and consent. Secret: `IAM_OIDC_CLIENT_SECRET`.
- Seeded registration client: `iam-registration`, client credentials with `client.create`.
  Secret: `IAM_DCR_CLIENT_SECRET`.
- DCR: POST `/connect/register` with an initial token; GET with the returned registration
  access token. Plaintext client secrets are returned only by registration POST.

The persistent signing key defaults to `.local/iam-signing-key.json`, mode 0600.
Set an absolute `IAM_SIGNING_KEY_FILE` for a stable deployment location. Do not
commit, regenerate, or rotate it just to restart the application. `IAM_ISSUER`
must match the external URL clients use.

## 5. Social providers

Google, GitHub, Facebook and Telegram use registrations `google`, `github`,
`facebook`, and `telegram`. Available reference credentials were placed in ignored
`.env.local`. A provider is registered only when both environment credentials exist.

Each provider dashboard must approve the exact callback URI. Defaults:

```text
http://localhost:9000/login/oauth2/code/google
http://localhost:9000/login/oauth2/code/github
http://localhost:9000/login/oauth2/code/facebook
http://localhost:9000/login/oauth2/code/telegram
```

Override `<PROVIDER>_REDIRECT_URI` for a registered public HTTPS callback/tunnel.
The reference's old tunnel addresses were intentionally not adopted as this project's
public identity. Live account consent/MFA and provider-dashboard callback acceptance
remain manual acceptance checks; automated tests cover starts, state/PKCE and
provider identity mapping without using real accounts.

Provider identity is keyed by provider/subject. Emails never silently merge accounts.
Linking requires an authenticated internal owner and server-bound OAuth state.
Missing mandatory email/names use a five-minute, server-stored profile-completion
flow. Telegram uses its supported `openid profile` scopes and signed ID token;
it does not supply a separate UserInfo endpoint or email scope.

## 6. Domain and data behavior

- BCrypt passwords; credentials never enter ordinary response DTOs or logs.
- Soft deletion, audit actors/timestamps, optimistic locking; assignments are revoked,
  never deleted. Existing `revocation_reason` history is preserved in addition to the
  Section B assignment reason. Assignment versions are technical concurrency columns.
- Effective permissions = active role permissions + direct GRANT − direct DENY.
- System roles/permissions are protected; inactive/deleted targets cannot be assigned.
- Batch assignment and creation with initial assignments roll back as a whole.
- Owner locks serialize assignment duplicate checks. Provider/subject advisory locks
  serialize first-time social provisioning; database uniqueness enforces ownership.
- One active identity per user/provider; unlinking the last passwordless sign-in
  method is rejected. Explicit relinking restores the original owner's identity row.
- Revoking connected applications removes persisted grants/consent and blocks refresh.
  Existing self-contained access tokens may remain valid until their expiration.
- PATCH distinguishes missing fields from explicit null. Stale browser profile edits
  return 409 and offer reload. Email/phone changes clear their verification flags.

## 7. Verify

Fast compilation/domain/provider-mapping checks:

```bash
./gradlew check
```

Complete integration and packaged-JAR checks:

```bash
./gradlew check :iam-main:bootJar -Ppart2Integration=true -PpackagedChecks=true
```

The tests create temporary PostgreSQL clusters on free ports, migrate and validate
all tables, use synthetic accounts, and stop/remove their clusters. They never use
the normal application database. PostgreSQL tools `initdb`, `pg_ctl`, `createdb` must
be on PATH, and tests must run as a non-root user. To supply a disposable server,
set `IAM_TEST_DB_URL` to a database named **iam_part2_test**, plus its username/password.
An explicitly supplied database is caller-owned and is not removed.

If you only have Docker PostgreSQL (no native PostgreSQL tools), create a separate
throwaway test database inside the review container and run:

```bash
docker compose exec -T postgres createdb -U iteuser iam_part2_test
export IAM_TEST_DB_URL=jdbc:postgresql://localhost:9544/iam_part2_test
export IAM_TEST_DB_USERNAME=iteuser
export IAM_TEST_DB_PASSWORD="$DB_PASSWORD"
./gradlew check :iam-main:bootJar -Ppart2Integration=true -PpackagedChecks=true
unset IAM_TEST_DB_URL IAM_TEST_DB_USERNAME IAM_TEST_DB_PASSWORD
```

Use this database only for tests. Creating it is needed once; if it already exists,
reuse only that disposable test database. Never point tests at `db_iam_service`.


The optional `browserChecks` flag references `scripts/browser-check.mjs`, which is
not included in the current working tree. Do not enable that flag in this checkout;
the browser screenshots in `verification` are historical evidence, not a replacement
for the current Java test results. Without the integration flag, `check` skips the
database suites. The Gradle wrapper is included; no global Gradle install is needed.

Earlier delivery evidence records **34 tests, 16 browser checks and 13 static contracts**;
packaged JAR renders React pages outside the source tree. Browser screenshots
and machine-readable reports are under `verification`; Gradle reports are under
`iam-main/build/reports/tests/test`. See [delivery evidence](docs/DELIVERY-AUDIT.md).

## 8. Resource provenance

Adapter patterns, translations and Postman resources were reused from the supplied
`istad-ite-1` project and adapted to this project's inner layers. See
[source references](docs/SOURCE-REFERENCES.md), [DESIGN.md](DESIGN.md), and
[UX-CONTRACT.md](UX-CONTRACT.md). No reference-project source or database was changed.

## Code style

Java source uses four-space indentation, explicit imports, and braces around
control-flow bodies. Use-case wiring is grouped by feature under
`iam-main/src/main/java/co/istad/iam/config`. Domain and application classes remain
framework-free. `.editorconfig` describes the whitespace convention.

### Password recovery

For local development without SMTP credentials, use the `local` Spring profile.
It sends messages to the Mailpit inbox at **http://localhost:8025**, rather than
to an external mailbox. The Docker service binds only to the local machine and
stores messages in its own named volume. With local secrets set in `.env.local`:

```bash
bash scripts/run-local.sh
```

For IntelliJ, set `SPRING_PROFILES_ACTIVE=local` in the IAM run configuration and
start the inbox with `docker compose --env-file .env.local --profile local up -d --wait mailpit`.
The local profile uses SMTP port 1025 without authentication or STARTTLS.
Do not copy the example real-SMTP `MAIL_*` values into a local-profile environment;
explicit environment values override the local defaults. The default profile keeps
authenticated, required-STARTTLS delivery for real SMTP.

To use Resend with the existing Spring Mail integration, set
`SPRING_PROFILES_ACTIVE=resend`, `RESEND_API_KEY`, and `IAM_MAIL_FROM` in your
local environment or secret manager, then restart IAM. This profile uses
`smtp.resend.com:587`, username `resend`, the API key as its SMTP password, and
required STARTTLS. Both verification and password-recovery messages use it.
Use a sender on a verified Resend domain for delivery to users. The fallback
`onboarding@resend.dev` sender is for testing with your Resend account email only.
See [Resend SMTP setup](https://resend.com/docs/send-with-smtp).

For Gmail delivery without a custom domain, enable Google 2-Step Verification,
create an [app password](https://myaccount.google.com/apppasswords), and run:

```bash
python3 scripts/configure-gmail.py
```

The setup prompts for the sender and app password without displaying the password.
It authenticates over required STARTTLS and sends a test message before saving
`SPRING_PROFILES_ACTIVE=gmail`, `GMAIL_USERNAME`, and `GMAIL_APP_PASSWORD` to
ignored, owner-only local files. It updates the existing private IntelliJ run
configuration too. Restart IAM afterward. The `gmail` profile uses the authenticated
Gmail address as sender and tests SMTP connectivity on startup. Verification,
recovery links, and password-reset notifications all use this same delivery path.
Test-message acceptance confirms Gmail accepted the send; check the recipient's
inbox or spam folder to confirm delivery. Never commit app passwords.

The sign-in page links to `/forgot-password`. Users enter their registered email,
receive a single-use link valid for 15 minutes, and set a new password at
`/reset-password`. Only existing local-password accounts are eligible; social-only
accounts continue using their provider. Responses do not reveal account existence.

Set `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `IAM_MAIL_FROM`
(see `.env.example`). The configured `app.iam.issuer` must be the public IAM URL;
links are never constructed from the request Host header. SMTP uses authentication,
required STARTTLS, and five-second network timeouts. Delivery failures are logged
without the recipient or reset token; the browser still receives generic feedback.
Never log query strings for `/reset-password` in a reverse proxy or access logger.

Tokens are SHA-256 hashed in PostgreSQL, replaced on a new request, and consumed
atomically with the password update. Requests are limited to once per minute per
account and per browser session. Add ingress/IP rate limits for distributed abuse
protection. Resetting removes stored OAuth authorizations (including refresh tokens)
and expires login sessions tracked by this IAM instance. Already-issued JWT access
tokens remain usable until expiry; multi-instance session revocation requires a
shared session store. Changing an account's email or password invalidates older
reset links. No password is emailed or automatically used to sign in.

### Email verification with Redis OTP

Registration creates an unverified user, stores a salted hash of a secure six-digit
code in Redis for five minutes, and sends the code via `EmailService` using Spring
`JavaMailSender`. Open `/verify-email` to submit the email and code. Codes work once;
five wrong attempts invalidate them. `/resend-verification` replaces the previous
code, with a sixty-second cooldown. Old email-verification links no longer work.

Start Redis using `docker compose --env-file .env.local up -d --wait redis`.
The app uses `REDIS_HOST` (default localhost), `REDIS_PORT` (6379), and optional
`REDIS_PASSWORD`. Use the `gmail` profile with `GMAIL_USERNAME` and
`GMAIL_APP_PASSWORD` for Gmail SMTP, or `local` for captured mail in Mailpit.
Delivery failures leave the account pending and allow a resend. An unavailable
Redis server cannot approve verification. Existing SQL verification-token rows are
unused; the historical table is retained to avoid rewriting migration history.

Integration tests start disposable Redis Docker containers and PostgreSQL clusters;
Docker must be running. No live mailbox is used by these tests.

### React browser UI

Browser pages now use React instead of JTE. Spring Boot serves them on port 9000,
including login, account settings, recovery, verification and OAuth consent. JavaScript
is required. Existing Spring MVC controllers retain form processing, redirects and
CSRF/session protection. `iam-web/REACT-MIGRATION.md` documents the rendering contract.

`./gradlew :iam-main:bootJar` automatically downloads pinned Node.js, installs locked
npm dependencies and includes the React build. After changing frontend code, rebuild
and restart Java. For IntelliJ, run `:iam-web:processResources` before starting the
application, or delegate builds to Gradle.

Run `./gradlew check -Ppart2Integration=true -PbrowserChecks=true -PpackagedChecks=true`
for integration, real-browser and packaged-JAR checks. Install Chromium once with
`npx playwright install chromium` from `iam-web/iam-web-reactjs`.
