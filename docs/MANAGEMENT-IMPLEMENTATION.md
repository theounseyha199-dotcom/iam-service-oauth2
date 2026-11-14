# Management implementation checklist

This extends the existing React/Tailwind workspace and permission-checked REST API.
Each item is verified against real persisted data, with disposable databases for tests.

| Requirement | Starting point | Work in this implementation |
| --- | --- | --- |
| Admin overview | Permission-filtered totals; personal recent activity | Show system activity to audit readers |
| Users | Create, read, edit, delete, enable/disable, lock/unlock | Make current security status and relevant actions clear |
| User roles | Assign/revoke with reasons; effective role listing | Improve selection without removing permission checks |
| Direct permissions | Grant/deny/revoke with reasons | Reuse the assignment flow |
| User sessions | Per-user list and one/all termination | Verify ownership and administrator permissions |
| Login history | Successful/failed attempts and device metadata | Add an explicit repeated-failure indicator |
| Roles | CRUD and permission assignment/revocation | Show assigned permissions to read-only administrators |
| Permissions | CRUD with resource/action fields | Retain existing domain validation |
| Clients | CRUD, secrets, grants, redirects, scopes, token lifetimes | Verify current behavior and secret handling |
| Account security | Password reset, unlock, activation controls | Complete deactivation review and session revocation |
| Audit logs | Event/result filters and paging | Add actor, target, date and repeated-failure filters |
| System configuration | Password rules, idle timeout, token defaults, consent | Verify applied policies and optimistic concurrency |

## Deactivation review contract

Requests are pending, approved or rejected. A reviewer needs `user:read` to see the
queue and `user:update` to decide. Decisions require a reason and the current
request version. Concurrent or repeated decisions return 409. Administrators cannot
review their own request. Approval disables the account, revokes stored OAuth
authorizations and expires its active browser sessions after commit. Rejection
keeps the account active and visible with its review note. A rejected owner can
submit a new request. The server records each decision and its actor.

## Activity contract

The existing subject column identifies the actor. New activity rows also retain a
bounded target path without query strings or request bodies. Historical rows do
not acquire fabricated target metadata. Date filters cover UTC calendar days.
Repeated failed logins mean at least five failures for the same account during
the preceding fifteen minutes, including the displayed attempt; this is an
indicator for review, not proof of malicious activity.

## Verification

Run the frontend lint/build and form-schema checks, focused HTTP integration tests,
and browser management flows. Verify forbidden operations, stale decisions,
invalid filters, state persistence, translations and narrow-screen behavior.

## Completed verification

- Full `./gradlew check -Ppart2Integration=true -PbrowserChecks=true` passed.
- Deactivation tests cover permissions/CSRF, required reason/version, stale/repeated
  decisions, self-review rejection, rejection/resubmission, session expiry, access-
  token refusal and stored-authorization revocation after approval.
- Audit tests cover actor/target/date/result filters, literal wildcard searches,
  pagination bounds, repeated-failure indicators and cross-account isolation.
- Browser checks cover role search/assign/revoke, admin review, owner decision
  visibility, English/Khmer mobile layouts and the existing CRUD/security flows.
- Frontend lint, production build, schema/resource-parity checks and the strict
  shared-UI audit passed. Packaged-JAR smoke testing remains opt-in.

The application applies migration `012-management-review.sql` on restart. Existing
requests become pending; historical activities retain their original metadata.
