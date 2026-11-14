# IAM browser behavior contract

Authority: assignment Sections B, D, E and F. Security/domain rules take precedence
over visual defaults. Applies to /login, /register, consent and all profile pages.

## Canonical UI Map

| Capability | Canonical owner | Source of truth | Allowed variants | Verification |
| --- | --- | --- | --- | --- |
| Form | React Field and Form in iam-web-reactjs/src/components/account | PDF Section F | register / edit / password | HTTP and browser tests |
| Select/Listbox | native select | DESIGN.md | locale / gender | keyboard and narrow viewport |
| Date | native date input | ISO date in PDF B.1 | birthdate | form round trip |
| Scrollbar | main.jsx document Tailwind utilities | DESIGN.md | document / horizontal navigation | CSS audit |
| Toast | React components/Notice.jsx | message bundles | success / error | status/alert assertions |
| CRUD | profile controllers + application use cases | PDF C/F | redirect on save / preserve on failure | integration tests |
| Confirmation | components/account/Confirmation.jsx | ownership and last-method rules | full-page confirmation | CSRF/ownership tests |
| Unsaved navigation | components/UnsavedChangesGuard.jsx | this contract | in-app dialog / tab beforeunload | browser test |

## Workflows

- Registration: validate, preserve non-secret fields on failure, redirect to the
  verification-code form with the registered email held in session state. Never auto-elevate privileges or echo password fields.
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
  Linking failures use one-time session-owned messages for cancellation, provider conflicts,
  expired linking sessions, and provider verification failures. Callback text and URL parameters
  cannot choose the displayed failure reason. A cancelled attempt retains the signed-in owner.
  Invalid-state callbacks also retain an existing internal IAM session while rejecting
  the callback; they must never create or link a provider identity.
  Each saved provider identity shows its provider icon/name, Connected badge, and
  Disconnect action. Its Connect button is hidden. Reloading retains this state
  from persisted identities; a success query parameter alone never marks a provider connected.

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

Password recovery (requested account-management flow): /forgot-password and
/reset-password reuse shared login layout, fields, CSRF, notices and busy states.
Valid email submissions use identical generic feedback. Reset links expire after
15 minutes and work once; invalid links offer a new request. Secret link parameters
are exchanged for session state before rendering or language switching.

Email verification (Redis OTP): registration commits a pending user before sending
mail through EmailService/JavaMailSender. `/verify-email` accepts email + a masked,
six-digit code. The code is never echoed into bootstrap data or URLs. Redis stores
only a salted hash with a five-minute TTL; five wrong attempts consume the code.
Resends have a shared sixty-second account cooldown and a browser/IP backstop;
a new issuance replaces the previous code. Redis scripts make issuing, retry counts
and consuming atomic. SMTP failure conditionally removes that issuance so a retry
can recover; the account remains pending. Redis failure cannot verify an account.
Existing verification links are retired. The historical SQL token table remains
for migration compatibility and is no longer used. Legacy accounts keep their
existing sign-in policy. Verification never changes administrator enable/disable
decisions. English/Khmer pages reuse shared React Field/Form/Notice components.

## Workspace API and administration

`/` and `/workspace` render the React workspace. `?panel=` selects overview,
sessions, history, activity, deactivation, reviews, users, roles, permissions, clients, audit,
or policy. Management navigation is derived from effective permissions. The server
recomputes browser API permissions for each request; navigation visibility does not
grant access. The browser uses same-origin session cookies and CSRF headers at
`/workspace/api/**`. Existing bearer-only `/api/**` authentication stays stateless.
User/role/permission controllers expose browser aliases with the same validation,
use cases, and method authorization as their bearer routes. New console operations
also expose bearer routes at `/api/console/**`, requiring an internal user subject.

Workspace inputs, forms, tables, paging and feedback are owned by `components/workspace`;
feature workflows live in `features/account` and `features/management`. WorkspaceProvider
owns API/context state, and use-workspace-confirmation owns confirmation behavior.
Localization is owned by `workspace-messages.js`.
Read-only account pages retain their established native form primitives. Admin
profile edits send the loaded version; stale saves return 409. Username edits use
the same profile transaction and uniqueness checks. Client secrets remain masked,
are never returned by reads, and are retained when an edit leaves the field blank.

Account deactivation records a request for administrative review; it does not
silently disable the requesting account. Session termination checks ownership or
`user:update`; viewing another user's sessions/history requires `user:read`.
Audit requires `audit:read`; client and policy operations use separately seeded
permissions. Token defaults and consent policy apply to newly created clients.
Session idle timeout applies on the next authenticated request. Password settings
are enforced by the password hasher for registration, creation, changes and resets.


## shadcn dashboard shell

The workspace uses `components/DashboardShell.jsx` and generated shadcn/ui
primitives from `components/ui`. SidebarProvider/Sidebar own expanded, icon-only,
and mobile Sheet states. Mobile navigation closes after selection, restores focus
on Escape, and has localized names. Sidebar keyboard shortcuts ignore text entry
and IME composition. Language changes preserve the current client-side panel and
filters; links continue to participate in the existing unsaved-change guard.

The workspace Field/ActionForm/Table retain API and validation ownership while
rendering shadcn Button/Input/Table primitives. Overview counts use shadcn Card;
role indicators use Badge. The native destructive confirmation dialog retains
safe initial focus, duplicate-submit protection and retry behavior. Account and authentication pages retain their canonical controls and geometry
through Tailwind utility classes.


## Match shadcn to existing IAM components

The shared components/account Field/Form/Submit/Link wrappers remain the canonical account workflow API.
Field uses shadcn Input with `density="account"`; Submit uses shadcn Button with
`size="account"`. Provider actions, password visibility, consent actions and the
unsaved-change dialog reuse Button. Account cards use shadcn Card rendered as
semantic sections. Dashboard controls use the same primitives with compact sizes.
`components/Notice.jsx` is the single feedback owner for account messages and
workspace failures; callers supply localized text and maintain success/error roles.
No client-side routing is substituted for native authentication or consent POSTs.


## Route layout ownership

DashboardShell is rendered exactly once by App for every accountPages route.
Workspace renders panel content and its confirmation dialog; it does not own a
second shell. dashboard-navigation.js owns menu items, active account-page mapping
and management permission visibility. React Router v7 owns URL changes and Back/Forward navigation. use-workspace-route.js
adapts useLocation/useSearchParams for shared headings and workspace content. Direct loads,
reloads, saves and cross-page navigation must preserve the same sidebar/header and
viewer/permission projection. Authentication/recovery retain a separate layout.
Regression coverage navigates all account routes, asserts one shell, unchanged
sidebar width, active navigation and management availability, then returns to an
absolute workspace panel URL. Existing dirty-navigation and consent tests remain.


## Account completion contract

Authority: the user's eight Account requirements and approved implementation plan.
Profile views show saved values separately from the draft picture preview; broken
images fall back to initials. Password/reset and social ownership rules remain as
specified above. Provider connection actions exclude already linked providers.
`/deactivation/mine` always derives its subject from the authenticated actor and
never requires administrative read access. Resubmission updates one pending request;
requesting deactivation does not disable an account. Reviewers with `user:read` and
`user:update` can approve or reject another user's pending request with a reason
and the current version. Approval disables the account, expires its sessions and
revokes stored OAuth authorizations. Existing user access tokens check current
account availability. Rejection keeps the account active and lets the owner resubmit.
Session deletion expires exactly the selected subject's session(s). On its next
request an expired session cannot access protected content; workspace APIs return
401, and browser pages return to sign in. Session ownership is tested across two
clients and different users. Location uses a local GeoIP database only; no external
IP disclosure or inferred city from timezone is permitted. Device names are
untrusted display hints. Profile/password/unlink actions record their actual outcome;
password reset and provider linking emit completion events after transaction success.


## React Router and component ownership

`router.js` defines explicit application routes and lazy page imports. The root
loader reads the existing safe server bootstrap on navigation, so the server keeps
ownership of authentication, permissions, localized messages and CSRF. Every
account path/locale change revalidates; same-workspace panel/filter/page changes
use React Router search parameters without reloading the bootstrap. Redirects
(including expired sessions) follow server destinations. Server error views render
the localized error page instead of a mismatched route.

`App.jsx` composes providers/layouts and an Outlet. layouts owns auth/account
composition; components/account owns native account form controls; components/workspace
owns API-backed controls; features owns the workflows. UnsavedChangesGuard uses
useBlocker for Router links and Back/Forward, plus beforeunload for document exits.
AppLink uses React Router only for known application pages; provider authorization,
external destinations and other protocol endpoints use native anchors. All account
authentication/consent POSTs retain native CSRF-protected browser submission.
Outgoing route content keeps its footprint and becomes inert/invisible while the
next server bootstrap loads, preventing edits to a form about to be replaced.


## Form state and translation ownership

React Hook Form owns shared account/workspace form values, validation state, field
registration, focus and resets. Zod schemas under `src/schemas` are the client
validation contract; server validation and authorization remain authoritative.
Native account POSTs execute only after a successful resolver result and retain
CSRF, hidden protocol fields and selected scopes. Invalid account submissions
clear masked secret/code fields through resetField while preserving non-secret
values and error hints. Workspace API errors map to registered fields via setError;
API mutations continue to use the existing confirmation and duplicate-submit guards.

i18next/react-i18next owns client translation. The server-owned account message
bundles hydrate the account namespace; English/Khmer workspace JSON resources own
the workspace namespace. Locale changes follow router/server state. Schema errors
are stored as translation keys and rendered in the active language. Password
visibility icons use Lucide with the existing localized accessible names.

## Management review and audit filters

The `reviews` panel is visible to `user:read`; review decisions additionally require
`user:update`. Review requires a reason, an explicit version and confirmation.
Administrators cannot decide their own requests. A stale/repeated decision returns
409. Approved requests expire browser sessions after commit and disable user bearer
access immediately. Rejected owners see the decision and can submit a new request.

Activity filters retain actor, target, UTC date range, result, event and repeated-
failure state in the URL. Language switching preserves these filters. All query
parameters are bounded and parameterized; personal activity never widens to another
actor without administrative permission. Repeated failures mean five failed logins
for an account in the preceding fifteen minutes; the text does not imply a confirmed
attack. Historical activities without target metadata remain readable.

Role and permission selection uses list/search/paging within the owning assignment
form. Picker paging/search buttons never submit the assignment. Canonical GUIDs,
including seeded IDs, use the same validation contract as the Java API. Read-only
role administrators can inspect assigned permissions without mutation controls.
