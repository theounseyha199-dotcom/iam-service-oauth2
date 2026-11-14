# React IAM workspace integration

Open `/` or `/workspace` after signing in. The dashboard now uses shadcn/ui with
a collapsible desktop sidebar, mobile navigation drawer, breadcrumb header,
language controls, metric cards and compact tables. The existing account and API
flows are retained. The existing Gradle build compiles and
packages `iam-web/iam-web-reactjs`; a separate frontend server is unnecessary.
Migration `011-workspace.sql` adds activity, session metadata, deactivation requests,
policy storage, and seven client/audit/policy permissions assigned to ADMIN.
Existing users must hold the relevant permissions to see management navigation.

| Requirement | Integrated path |
| --- | --- |
| Profile, username, email, names, phone, picture | `/profile/edit`, existing version-checked profile use case; picture uses a URL |
| Password changes and recovery | `/profile/password`, `/forgot-password`, existing single-use email reset |
| Social linking and unlinking | `/profile/accounts`, existing explicit provider OAuth flows |
| Sessions and termination | `/?panel=sessions`, own sessions or administrator user details |
| Login history | `/?panel=history`, successful/failed filters and user details |
| Account activity | `/?panel=activity`, persisted HTTP changes and login events |
| Deactivation request | `/?panel=deactivation`; recent requests shown to user administrators |
| Overview | `/?panel=overview`, permission-scoped counts and recent own activity |
| User CRUD, activation, lock/unlock, password reset | `/?panel=users` and user editor |
| Roles and direct permissions | User editor; enter the UUID from the role/permission list, confirm assign/revoke |
| Role CRUD and permissions | `/?panel=roles`, role editor and permission assignment |
| Resource/action permission CRUD | `/?panel=permissions` |
| OAuth2/OIDC clients | `/?panel=clients`, credentials, grants, redirect URIs, scopes and lifetimes |
| Audit | `/?panel=audit`, event search, success/failure filters, pagination |
| System policies | `/?panel=policy`, password rules, session timeout, token defaults, new-client consent |
| English and Khmer | Header language controls; workspace URL state is preserved on language changes |

The browser calls `/workspace/api/**` using the existing session cookie and a CSRF
header. The established `/api/users`, `/api/roles`, and `/api/permissions` endpoints
retain bearer authentication. Console operations are also available under
`/api/console`. Responses never return client secrets or raw HTTP session IDs.
Authorization changes are checked against current permissions for every browser API
request, including requests from an already authenticated administrator.

Verification:

```sh
npm --prefix iam-web/iam-web-reactjs run lint
npm --prefix iam-web/iam-web-reactjs run build
./gradlew check -Ppart2Integration=true -PbrowserChecks=true
```

Integration tests provision disposable PostgreSQL and Redis services. Browser
checks use the installed Playwright Chromium. They do not alter the normal
application database. Screenshots and logs are saved under `verification/`.

Operational limits:

- SessionRegistry tracks the current server process. Cluster-wide session views
  require shared session storage; historical sessions from before startup are absent.
- Session locations use an optional offline GeoIP2/GeoLite2 City database. Set
  `IAM_GEOIP_DATABASE` to its absolute `.mmdb` path and restart. Public addresses
  show approximate city/country when present; loopback/private addresses show
  Local network; missing data shows Not available. No IPs are sent externally.
  The remote socket IP is recorded;
  proxy forwarding requires deployment-specific trusted-proxy configuration.
- History starts when this migration is installed. Audit captures application HTTP
  mutations and authentication events; it does not cover out-of-band SQL changes.
- Provider linking needs the existing Google/GitHub/Facebook/etc. credentials and
  callback configuration. Email password recovery needs configured SMTP delivery.
- Password policy applies when a password is created or changed, and does not
  retroactively reject existing hashes. Token defaults do not rewrite existing
  client configurations or invalidate already issued access tokens.
- Deactivation requests remain in the review queue; an administrator can disable
  the user from user management. Users can view and update their own pending request at `/workspace/api/deactivation/mine`.
  Automated approval and request-resolution status are not implemented. The overview
  shows the most recent 100 requests.


## Account workflow completion

- Profile summary displays saved name, username, email and phone. A shared avatar
  displays the saved HTTP(S) picture URL in the profile, sidebar and overview;
  editing shows a live preview and initials fallback for broken/unsupported URLs.
  Duplicate username/email errors attach to their fields; optimistic versions are retained.
- Existing CSRF-protected password change/reset and social link/unlink workflows
  remain canonical. Security now links directly to password recovery. Already linked
  providers are excluded from connection actions and display a Connected status.
- Session/history tables show browser, operating system and device type instead of
  raw User-Agent strings. Expired workspace sessions return 401; other devices stay
  signed in when one session is ended. Ending all includes the requesting device.
- Profile/password/unlink/session/deactivation attempts have named activity events.
  Completed email password resets and provider links are recorded after success;
  federated sessions are registered under their final IAM principal for session management.
- Deactivation shows pending status, time and reason after reload; edits update the
  single existing request. Confirmation explains that the account stays active.

Additional checks: `node scripts/account-unit-check.mjs`. GeoIP tests use MaxMind's
small synthetic fixture under test resources; this is never a production database.


## Router/component structure

React Router v7 now owns frontend navigation. `src/router.js` defines the routes;
`lib/page-loader.js` refreshes authenticated/translated bootstrap data from the
existing MVC endpoints. Native authentication/consent POST and OAuth endpoint
behavior remain unchanged. `App.jsx` and `Workspace.jsx` are composition entries;
shared controls live under components, route views under pages, and account/admin
workflows under features. Resource field groups have separate components. Browser
coverage asserts document identity across account navigation, Back/Forward behavior
and unsaved-draft protection in addition to the existing CRUD/account checks.


## React Hook Form, Zod and i18next

Shared forms now use React Hook Form with explicit Zod workflow schemas. Native
account submission and JSON workspace mutations preserve their existing backend
contracts. Error association, first-invalid focus, confirmation and busy behavior
remain in the shared components. Forms use library resets rather than mutating
validation messages through DOM selectors.

i18next/react-i18next provides React translation and callback translation. Account
resources come from the server bootstrap; workspace English/Khmer resources are
JSON files. React Router locale transitions refresh the correct namespace.
Additional checks: `node scripts/frontend-stack-check.mjs`.
