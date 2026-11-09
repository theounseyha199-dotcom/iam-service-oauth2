# Reviewer guide

Start with [the README](../README.md). It describes a new-machine setup without
private credentials, a local reference repository, or pre-existing database tables.
Use root `compose.yaml`; the old deployment folder serves the author's other projects.

## Suggested review order

1. Check `settings.gradle` and each module's `build.gradle` for dependency direction.
2. Read `iam-domain/iam-domain-core` for domain models, assignment history and authority rules.
3. Follow one use case (for example CreateUserUseCase) from its command and output
   ports through the persistence adapter, mapper, entity and Spring Data repository.
4. Open `iam-main/src/main/resources/db/changelog/changes/changelog-master.yaml`
   for the migration order; schema changes are applied by Liquibase.
5. Inspect UsersController and the API security configuration for thin controllers,
   safe response mapping, JWT validation and exact permission requirements.
6. Read the infrastructure security/client configuration for PKCE, scoped claims,
   persisted clients/grants and the two accepted Postman callbacks.
7. Inspect the JTE templates and English/Khmer bundles in `iam-web`.
8. Run the README's verification command and import the portable Postman files.

## Minimal acceptance flow

- Start PostgreSQL and the application using your own chosen secrets.
- Sign in as the startup administrator. Obtain an OAuth token through Postman.
- `/api/me` and `/api/users` return 200 with the administrator token.
- Register a separate user through `/api/auth/register` and authenticate that user.
- `/api/me` returns 200; `/api/users` returns 403 for the normal USER.
- Without an access token, protected APIs return 401.
- Use the collection's role/permission/assignment requests for administrative CRUD.
  Fill record UUID variables from responses; do not paste authorization codes into
  Bearer Token fields. Access tokens expire in five minutes.

Real social-provider sign-in requires your own provider credentials and callback
approval. The ordinary local login, API and automated Java tests do not depend on
live provider accounts. The optional browser automation script is absent in this
checkout; historical browser screenshots are not claimed as a new run.

## Before sharing the repository

Many implementation files were untracked at review time. Review `git status`, add
and commit all intended source, migrations, documentation, Compose and portable
Postman files, then push that commit. A remote clone contains committed files only.
Private run configurations, private Postman environments, `.local` signing keys,
passwords, tokens and build outputs must remain excluded. The guide does not commit
or publish changes on the author's behalf.
