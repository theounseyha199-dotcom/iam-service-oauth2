# React browser architecture

JTE has been removed. All browser pages render in React, served by Spring Boot on
port 9000. The existing Java controllers still own validation, sessions, use cases,
CSRF-protected form posts, OAuth/OIDC redirects and Redis verification codes and reset-token exchange.
Redis is configured independently; this migration does not change session storage.

## Build and run

```bash
./gradlew :iam-main:bootJar
```

The Gradle Node plugin downloads pinned Node.js, runs `npm ci`, builds Vite and
packages the result under `static/react`. Run the Java app with the normal local
environment, then open `/login`. `/test` redirects to `/login`.

## Rendering contract

`ReactViewConfig` resolves an explicit allowlist of browser view names. It loads
Vite's generated entry so JavaScript/CSS hashes stay in sync. Controllers provide
an allowlisted model; the renderer projects display fields and injects escaped
JSON, localized message bundles and a CSRF token into the entry document.
`src/App.jsx` renders the requested page with shared Field, Form, Notice and
Confirmation components. Static assets use `/react/`; existing browser routes
remain unchanged. JavaScript is required.

Forms use native navigation to the existing POST endpoints. This preserves OAuth
saved requests and server validation while React owns controls and feedback.
Redirects remain Spring views. JSON REST endpoints remain separate and unchanged.
No catch-all SPA route captures OAuth, APIs or missing resources.

## Security and localization

- Retain HttpOnly session cookies and Spring CSRF validation.
- Never serialize a full MVC model, principal, entity, or password.
- Escape `<`, `>`, `&`, and Unicode line separators in embedded JSON.
- Use `Cache-Control: no-store` and `Referrer-Policy: no-referrer` on page responses.
- Email verification uses a six-digit OTP with Redis TTL/attempt limits; password-reset links still exchange their token for server session state.
- English/Khmer message bundles and locale cookie remain authoritative.
- `iam-web-reactjs/src/tailwind.css` owns Tailwind v4 theme tokens. JSX utilities
  own all page and component styling; the server does not inject legacy CSS.

## Verification

```bash
./gradlew check -Ppart2Integration=true -PbrowserChecks=true -PpackagedChecks=true
cd iam-web/iam-web-reactjs
npm run lint
```

Install the Playwright browser once using `npx playwright install chromium` from
the React directory. Integration checks use disposable PostgreSQL clusters and Redis Docker containers (Docker must be running).
Browser checks cover login, localization/mobile, profile save, unsaved navigation,
empty lists, logout and validation. Packaged checks launch the JAR outside the
source tree. Live third-party provider callbacks still need provider credentials.
