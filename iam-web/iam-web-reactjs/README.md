# ISTAD Identity browser UI

React renders the IAM pages, including sign-in, registration, profile, linked
accounts, connected applications, verification, recovery and consent.

Build from the repository root:

```bash
./gradlew :iam-main:bootJar
```

Gradle provisions Node.js, runs `npm ci`, builds Vite and packages its output.
Start Spring Boot with your local environment and open `http://localhost:9000/login`.
Rebuild and restart Java after changing frontend code.

`src/App.jsx` composes layouts, feedback and the routed Outlet. Java's `ReactViewConfig` supplies
localized, escaped bootstrap data and CSRF tokens. Forms post directly to the
existing Spring MVC/Spring Security endpoints. The backend owns authentication,
validation, authorization and protocol redirects.

Styling uses Tailwind v4 utility classes in JSX. `src/tailwind.css` contains only
Tailwind imports, variants and theme tokens. Do not add custom CSS selectors or
inline style objects. `src/main.jsx` applies global document utilities; shared
controls in `src/components/ui` own focus, density and disabled states.
See `../REACT-MIGRATION.md` for architecture and verification commands.

For frontend checks, run `npm run lint`. For browser checks, install Chromium using
`npx playwright install chromium`, then run the documented Gradle integration checks
from the repository root. Vite's standalone entry is not the full application:
browser pages require Spring Boot's model and session context.


## Frontend structure

```text
src/
  App.jsx                    application composition
  Workspace.jsx              workspace composition
  router.js                  React Router v7 routes and lazy page loading
  pages/                     sign-in, profile, consent, recovery and other route pages
  layouts/                   shared account and authentication layouts
  components/
    ui/                      shadcn/ui primitives
    account/                 account fields, forms, providers and profile sections
    workspace/               API-backed forms, tables, paging and dialogs
    dashboard/               sidebar navigation, header and page heading
  features/
    account/                 sessions, history, activity and deactivation
    management/              CRUD workflows, user tools and resource field groups
  context/                   shared account/workspace context
  hooks/                     resource loading, routing and confirmation state
  lib/                       API client, page loader, formatting and mappings
```

React Router owns links, search parameters and Back/Forward. Its loader refreshes
the safe server-projected bootstrap when the account route or language changes.
Workspace filter/panel changes stay in the router without redundant page requests.
Authentication and consent forms retain native server POSTs; provider authorization
and external links retain browser navigation. Unsaved edits are protected for both
client-side and document navigation.

Add a route page under `pages` and register it in `lib/route-paths.js` and `router.js`.
Keep presentation in small feature/shared components; use the existing primitives.


## Forms, icons and multilingual UI

- React Hook Form (`FormProvider`, registered fields, form-state errors) owns data
  entry. Zod schemas in `src/schemas` run through `@hookform/resolvers/zod`.
  Account and workspace forms declare their workflow schema and focus the first
  invalid field. API field errors are mapped back with `setError`; programmatic
  resets use React Hook Form's reset API.
- Account validation continues through the existing native Spring POST only after
  Zod succeeds. `submit-native-form.js` preserves clicked action values, CSRF
  inputs and native checkbox/hidden fields. Authentication/consent redirects and
  browser sessions remain server-owned. Secret fields clear on failed account
  validation and remain masked by default.
- Lucide React owns interface icons, including password visibility actions. Brand
  provider logos remain the existing local assets.
- `src/i18n/instance.js` initializes i18next and react-i18next. Workspace resources
  live in `src/i18n/resources/en/workspace.json` and `km/workspace.json`. The safe
  server message bundle loads into the `account` namespace on initial render and
  router revalidation. Java positional placeholders are adapted to named i18next
  interpolation. React text rendering provides output escaping.
- Locale remains governed by the server's English/Khmer URL/cookie choice. No
  client detector or storage is added. Validation, labels, accessible names and
  feedback follow that locale.

Run `node scripts/frontend-stack-check.mjs` from the repository root to verify
schema boundaries, password/OTP rules, translation interpolation and resource parity.

`npm run test:forms` runs these schema/i18next checks from the frontend directory.
Gradle `:iam-web:check` includes the same checks through `testReactForms`.
