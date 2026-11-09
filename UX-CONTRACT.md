# IAM browser behavior contract

Authority: assignment Sections B, D, E and F. Security/domain rules take precedence
over visual defaults. Applies to /login, /register, consent and all profile pages.

## Canonical UI Map

| Capability | Canonical owner | Source of truth | Allowed variants | Verification |
| --- | --- | --- | --- | --- |
| Form | shared/field.jte, shared/csrf.jte, account.js | PDF Section F | register / edit / password | HTTP and browser tests |
| Select/Listbox | native select | DESIGN.md | locale / gender | keyboard and narrow viewport |
| Date | native date input | ISO date in PDF B.1 | birthdate | form round trip |
| Scrollbar | account.css | DESIGN.md | document / horizontal navigation | CSS audit |
| Toast | shared/notice.jte | message bundles | success / error | status/alert assertions |
| CRUD | profile controllers + application use cases | PDF C/F | redirect on save / preserve on failure | integration tests |
| Confirmation | unlink-account.jte / revoke-app.jte | ownership and last-method rules | full-page confirmation | CSRF/ownership tests |
| Unsaved navigation | shared layout dialog + account.js | this contract | in-app dialog / tab beforeunload | browser test |

## Workflows

- Registration: validate, preserve non-secret fields on failure, redirect to sign-in
  with a success notice. Never auto-elevate privileges or echo password fields.
- Sign-in: preserve saved local destination. Invalid credentials use a generic error.
- Profile/password save: redirect back to the owning form with localized success.
  Validation preserves safe values; passwords are always cleared. Stale profile edits
  return 409 and offer reload without silently overwriting newer data.
- Unlink/revoke: GET only shows confirmation; POST requires CSRF and checks ownership.
  Safe cancel returns to the owning list. The last sign-in method cannot be removed.
- Consent: show client name and requested scopes. Allow/Cancel send the protocol
  response with the original client/state; language switching preserves that state.
- Language: ?lang=en|km persists a locale cookie; both bundles have identical keys.
- Provider login/link starts only after explicit user action. Provider callbacks must
  validate state and signed OIDC claims where applicable. Matching email is never
  automatic account linking. Provider credentials remain environment-only.

## States and accessibility

Shared controls provide keyboard focus, labels, help/error associations, disabled/busy
states, duplicate-submit protection and password visibility controls. Notices are
status/alert live regions. Empty linked-account/application lists explain what to do.
Form and scrollbar geometry remain stable. Browser-owned select/date popup behavior
is accepted. No modal is used for destructive confirmation; the full page provides
clear context. The dirty-navigation dialog uses native dialog focus containment,
Escape/cancel, least-destructive initial focus and focus restoration.

REST callers receive RFC 9457 Problem Details rather than browser HTML redirects.
JWT scope alone does not grant a Section D permission. Self-service identity is
always derived from the validated IAM access-token subject.
