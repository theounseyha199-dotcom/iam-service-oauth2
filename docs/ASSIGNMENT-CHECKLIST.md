# Assignment checklist

Source: the supplied PDF, in document order. This records implemented behavior and
test evidence, not a prediction of the instructor's awarded score.

| PDF requirement | Implementation | Evidence |
| --- | --- | --- |
| A: hierarchy and dependency direction | settings.gradle and module build.gradle files | clean compilation; static boundary check |
| B.1–B.4: User, Role, Permission, Identity | iam-domain-core models; separate persistence entities/mappers | fresh PostgreSQL schema validation and round trips |
| B.5: audited assignment tables, no ManyToMany | UserRoleEntity, RolePermissionEntity, UserPermissionEntity | assignment history and revocation tests; static scan |
| B.6: authority formula | UserAuthoritiesCalculator + UserApplicationSupport | role/direct GRANT/DENY test |
| B.6: protected system/inactive records | domain guards and assignment use cases | domain + integration tests |
| B.6: identity ownership and last sign-in method | IdentityApplicationSupport and identity use cases | ownership, unlink, relink tests |
| B.6: ADMIN/USER and permission seeds | changeset 007 | seed assertions |
| C.1: 19 user use cases | application/user/usecase | PartTwoIntegrationTest |
| C.2: 9 role use cases | application/role/usecase | PartTwoIntegrationTest + all REST routes test |
| C.3: 7 permission use cases | application/permission/usecase | CRUD, locking, filtering and REST tests |
| C.4: 4 federation use cases | application/identity/usecase | identity and provisioning tests |
| C.5: five required ports | application/port/out | adapters wired successfully |
| Part 2: Liquibase for migrations | main resources db/changelog/changes | all eight changesets on disposable databases |
| D.1–D.3: 39 REST endpoints | five restapi controllers | FullAssignmentIntegrationTest exact-payload route workflow |
| D.4–D.5: DTOs, lists, Problem Details | Requests/ProfileRequest/Responses, PageResult, ApiErrors | payload/page/error assertions |
| D: bearer security and exact permissions | ApiSecurityConfig + method security | own JWT, no-token 401, role-only 403 and tampered token tests |
| E.1: three JDBC services | SecurityConfig and JdbcAuthorizationPersistence | actual code/token/consent/refresh lifecycle |
| E.1: UserDetails adapter | UserDetailServiceImpl → LoadUserForAuthenticationUseCase | actual browser/form login |
| E.2: sub/claims/authorities | TokenConfig | decoded JWT, UserInfo and scoped ID claims assertions |
| E.3: dynamic client registration | OIDC registration endpoint + ClientRegistrationPolicy | initial token, POST 201, registration-token GET and scope rejection |
| E.4: four providers | SocialProvidersConfig, mapper, state repository, login handler | four starts, PKCE/state and seven mapper tests |
| F: all eight pages | iam-web/src/main/jte | HTTP page checks + precompiled JAR verification |
| F: English/Khmer and lang switch | messages.properties/messages_km.properties, BrowserConfig/Page | key parity, locale HTTP and browser tests |
| Best practices: inner layers, single-responsibility use cases | application classes and output ports | static audit |
| Best practices: audit, soft delete, version, environment secrets | domain/persistence/configuration | locking/deletion tests and static config audit |
| Best practices: tests, README, Postman | test suites, root README, postman directory | Gradle checks; Postman JSON validated |

Additional browser-support use cases implement consent display and connected-app
listing/revocation without introducing Authorization Server types into inner layers.

Remaining external acceptance: real Google/GitHub/Facebook/Telegram account consent,
MFA, and exact provider-dashboard callback approval. No live account authentication
or external dashboard mutation is claimed by automated tests.
