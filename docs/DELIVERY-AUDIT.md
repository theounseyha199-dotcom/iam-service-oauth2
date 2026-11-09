# Delivery audit

> Current clone-readiness review: use the root README and root `compose.yaml`.
> Java integration/packaged tests were rerun; browser checks were not rerun because
> their optional script is absent. The 16 browser checks below are earlier evidence.
> Commands referencing `scripts/` below describe that earlier run and are not current
> setup instructions. See `verification/clone-readiness.json` for the current results.

Verified 9 November 2026 against the supplied assignment PDF, Sections A–F.

## Results

| Check | Result |
| --- | --- |
| Required application use cases | 39, plus 3 browser consent/application helpers |
| Required REST endpoints | 39 |
| Required JTE pages | 8, plus error/confirmation/completion support pages |
| Separate JPA entities | 7; no ManyToMany |
| Liquibase changesets | 8 applied successfully to fresh PostgreSQL databases |
| Automated tests | 34 passed; 0 failures, 0 errors, 0 skips in the complete run |
| Real Chromium browser checks | 16 passed; English/Khmer, keyboard, validation, account/password updates, mobile and unsaved navigation |
| Static assignment contracts | 13 passed |
| Strict UI contract audit | Passed, no findings |
| IDE build | Passed, no problems |
| Packaged JAR | Built and started outside the source tree; precompiled Khmer login returned HTTP 200 |
| Postman | 51 requests; JSON valid; committed environment secrets/tokens empty |
| Normal application database | Not used by verification |

Complete verification command:

```bash
./gradlew check :iam-main:bootJar -Ppart2Integration=true -PbrowserChecks=true -PpackagedChecks=true --offline --console=plain
python3 scripts/audit-assignment.py
```

Machine-readable evidence:

- [Test summary](../verification/test-summary.json)
- [Assignment contracts](../verification/assignment-audit.json)
- [UI contract audit](../verification/premium-audit.json)
- [Browser checks](../verification/browser/results.json)
- [English sign-in](../verification/browser/login-en.png)
- [Khmer sign-in](../verification/browser/login-km.png)
- [Mobile profile](../verification/browser/profile-mobile.png)

The main integration suite exercises the PDF request payloads across all required
REST endpoints; JWT 401/403, validation and not-found errors; code + PKCE, consent,
scoped claims, UserInfo, DCR registration/read, permission restrictions and refresh
rejection after consent revocation. Separate domain/persistence tests cover history,
DENY precedence, account status, password hashing/changes, rollback, concurrent
assignment, identity ownership, profile completion and last-method protection.

Browser automation also verifies password masking/toggling, first-error feedback,
registration, sign-in, profile save, native-select keyboard access, accessible dirty
navigation confirmation, password change, provider linking controls, app empty states,
and 390px layout without document overflow. No browser script errors or server HTTP
5xx were observed during these workflows.

## Manual acceptance remaining

Real Google/GitHub/Facebook/Telegram account consent/MFA and exact callback approval
in provider dashboards require the user's accounts. Automated tests use synthetic
credentials/accounts and validate starts and provider-proof mapping; they do not
claim live external account sign-in. Existing provider credentials were found in the
reference project and reused only in ignored local environment configuration.
The reference's previous public tunnel addresses were not adopted automatically.

No reference-project source, normal application database, external app registration,
or existing signing key was modified by verification. The local test processes and
temporary PostgreSQL clusters were stopped and removed. The Postman collection was
validated structurally; no separate Newman runtime pass is claimed.
