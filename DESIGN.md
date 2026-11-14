---
name: ISTAD Identity
colors:
  primary: '#364fc7'
  on-primary: '#ffffff'
  background: '#f8fafc'
  surface: '#ffffff'
  text: '#0f172a'
  muted: '#475569'
  border: '#e2e8f0'
  danger: '#dc2626'
typography:
  sans:
    fontFamily: 'Inter, Segoe UI, system-ui, sans-serif'
  mono:
    fontFamily: 'ui-monospace, monospace'
rounded:
  DEFAULT: '0.5rem'
  lg: '1rem'
spacing:
  section-gap: '1.25rem'
  page-max: '70rem'
components:
  button: {}
  card: {}
  field: {}
  notice: {}
---

# ISTAD Identity design system

## Overview

An institutional account desk for ISTAD students and administrators: clear identity,
plain language, and deliberate cobalt accents. The signature is a compact bilingual
header above an uninterrupted account workspace. It is a product UI, not a marketing
landing page. Avoid decorative glass, animated backgrounds, and oversized statistics.
The PDF supplies the authoritative workflows, permissions, English/Khmer policy,
and required pages. Reused reference account pages establish the visual baseline.

Runtime ownership: `iam-web/iam-web-reactjs/src/tailwind.css` owns the Tailwind v4
`@theme` palette and typography tokens. JSX utility classes own layout, states and
responsive behavior. `main.jsx` applies document-wide Tailwind utilities, including
scrollbars, Khmer typography, keyboard focus and reduced motion. This file documents
those accepted tokens; it does not independently generate CSS. Do not add custom
CSS selectors or inline style objects for React UI.

## Colors

Cobalt primary `#364fc7`, white surface `#ffffff`, slate background `#f8fafc`, ink
`#0f172a`, secondary text `#475569`, border `#e2e8f0`, danger `#dc2626`. Focus uses
brand blue with a visible outline; errors include text and never rely on color alone.
Global scrollbars use the same slate palette and remain visible.

## Typography

Body uses Inter when available, then system sans; Khmer uses Noto Sans Khmer,
Khmer OS System, then the system fallback with 1.85 line-height. No network font
request is required. Titles use restrained heavier sans. Identity subjects wrap
instead of truncating. Labels, errors, dates and controls use the active language;
platform-owned date/select popups follow the user's browser/OS settings.

## Layout

Account content max-width 1120px; authentication max-width 600px, with a 460px sign-in card. Profile sections
use two columns above 700px and one below. Long forms use document scrolling;
no viewport-height container clips them. Header/account navigation remains reachable
on narrow screens. Form controls are 50px high, and error regions reserve space. Authentication cards
use a restrained cobalt top edge, generous internal spacing, and a ruled provider
divider. Language links share one compact segmented surface. Field borders use
slate 300 at rest and a cobalt border with a pale blue focus outline.

## Elevation & Depth

Borders and spacing express hierarchy. Cards have white surfaces without ambient
blur or decorative shadows. Only the unsaved-changes dialog uses a backdrop.

## Shapes

Controls use 8px radius, cards 16px radius. Do not change these per screen.

## Components

Shared React layout, Field and Form retain validation, localization and native POST
ownership. They render the installed shadcn Button, Input and Card primitives.
Button's `account` size and Input's `account` density preserve the established
50px controls; dashboard controls retain their compact sizing. Card accepts a
semantic element so account sections retain their heading structure. Shared
feedback is owned by `components/Notice.jsx` for both account and workspace pages.
Primary buttons use cobalt; secondary actions use white; final destructive actions
use danger styling. Busy buttons retain dimensions and have `aria-busy` on forms.
Verification uses a masked six-digit field with numeric input mode and one-time-code autofill.
Inputs use visible labels, error descriptions, appropriate autofill and password
masking with an accessible show/hide action. Success/error notices use status/alert.

Native select and date controls are intentional; platform popup geometry is accepted.
Unlinking and consent revocation use separate confirmation pages with object names,
consequences and a safe cancel destination. Dirty in-app navigation uses one shared
app-owned dialog. Actual tab closure uses the narrow beforeunload mechanism.

### Iconography

Text labels carry meaning. Sign-in provider icons are local SVG assets paired with localized names. A compact cobalt monogram marks the sign-in card. The shared password field supports an inset eye toggle on sign-in, retaining localized show/hide accessibility labels.

### Motion

No ambient animation. Reduced-motion users get immediate state changes.

### Content and data visualization

All visible labels and feedback come from message bundles. English and Khmer have
matching keys. Usernames, client names, scopes and claims are data, not translation
keys. Do not render passwords, tokens, raw provider snapshots, or database errors.

## Do's and Don'ts

- Reuse the shared Field/Notice/Form components for every account page.
- Preserve non-secret input on validation failure, and focus the first error.
- Keep consent and permissions explicit; do not infer account ownership from email.
- Do not add native alert/confirm/prompt calls or bypass browser CSRF protection.

## Identity workspace

The React workspace uses the official shadcn/ui new-york components generated by
its CLI. `components/ui` owns sidebar, sheet, button, input, card, badge, table,
separator and tooltip primitives. `components/DashboardShell.jsx` composes the
persistent navigation, breadcrumb, language controls, identity footer and sign-out.
The desktop sidebar is 256px and collapses to icons; below 768px it becomes a Radix
Sheet with focus containment, Escape dismissal and localized close/navigation copy.

`src/tailwind.css` defines the established cobalt/slate palette with Tailwind v4
`@theme` tokens and imports Tailwind preflight and utilities. `features` and shared components use
Tailwind utilities for product layout, compact data density and page composition.
The workspace uses the full available width with a 1550px content cap. Individual
tables own horizontal overflow; forms retain document scrolling. Cards use a 10px
radius, restrained borders, compact counts and live API data. No illustrative
metrics or fabricated charts are introduced. Sidebar icons use Lucide; the shield
monogram and bilingual identity footer identify the IAM platform.

Workspace copy and accessible labels live in the English/Khmer JSON resources
under `src/i18n/resources`; `workspace-messages.js` is an i18next callback adapter. Dates and pagination use the active locale. Existing server
message bundles continue to own native account/authentication pages. Search,
filters, and pagination live in the URL. Native selects use browser-owned popups.
Create/edit returns to the owning list, preserving search/page. Access-changing
operations use one native modal dialog with safe initial focus, Escape, a busy
state, error recovery, and focus restoration. API errors expose localized summaries
and field names, never parser messages or secrets. Session keys shown to clients
are SHA-256 digests rather than authentication cookie values.


## Existing-component adapters

Shadcn is adapted to the existing IAM component contract. Account page structure,
labels, form actions, validation, CSRF, consent, success destinations and password
visibility remain owned by the components/account Field/Form/Submit/Link wrappers. The generated
primitives provide common focus, disabled and visual variants. Account JSX utilities retain accepted layout and dimensions; the shared
primitives own account density and Notice owns message layout. Native date, checkbox and select semantics are preserved.
`components/Notice.jsx` provides the shared success/error surface, icon, live
region and error focus target without changing localized message text.


## Unified signed-in layout

App and AccountLayout own one DashboardShell for the overview, management panels and all authenticated
account pages, including profile, password, linked accounts, connected apps and
consent. Route changes replace the content, active item and page heading; the
sidebar, header, gutters, language controls and identity footer retain their shared
owners. Sidebar collapse persists in its existing cookie. Standalone sign-in,
registration and recovery keep the authentication layout.

`lib/dashboard-navigation.js` owns navigation and permission-based visibility.
`hooks/use-workspace-route.js` is the URL subscription used by both the shell and
workspace panels, including browser Back/Forward. Account-page links to workspace
panels use absolute application paths, so they cannot resolve under `/profile/*`.
ReactViewConfig projects the same safe viewer and effective permission data for
every authenticated account view before the first render. Account forms keep their
existing native CSRF-protected actions and unsaved-change guard inside the shell.


## Tailwind styling migration

All React screens use Tailwind utilities. The server no longer injects the legacy
account stylesheet. `App.css`, `Workspace.css`, `shadcn.css` and the unused Vite
starter stylesheet are retired. Semantic class names retained in JSX serve browser
test hooks and contextual Tailwind variants; they have no custom CSS rules.
Sidebar dimensions and skeleton width use utilities rather than inline styles.
This changes styling ownership while preserving the established visual identity
and account/workspace behavior contracts.


## Account workflow details

`components/ProfileAvatar.jsx` owns profile/sidebar/overview image geometry and
initials fallback. It accepts only absolute HTTP(S) image URLs without embedded
credentials and uses a no-referrer image request. The profile summary shows saved
values; the picture field has a separately labelled draft preview. Existing account
control dimensions, cobalt/slate tokens and Tailwind-only styling are preserved.
Session and history metadata uses a shared device parser, translated device kinds,
and an explicit approximate/local/unavailable location state. Deactivation shows
one persistent pending-review panel with the recorded time and reason; confirmation
copy describes review rather than immediate account disabling.


## Component structure and routing

The frontend uses React, shadcn/ui and React Router v7. Existing visual tokens and
Tailwind utility styling remain unchanged. App and Workspace are composition entry
points. `pages` contains route screens; `layouts` composes auth/account surfaces;
`components/account`, `components/workspace` and `components/dashboard` own reusable
controls/sections. `features/account` and `features/management` own API-backed flows;
`hooks`, `context` and `lib` own state, access, requests and pure mappings. Pages are
lazy-loaded. Keep new screens and resource-specific field groups out of entry files.


## Form and language adapters

Shared account/workspace controls register with React Hook Form and show localized
Zod errors in the established reserved error rows. Field dimensions, typography,
colors, focus treatments and Tailwind ownership remain unchanged. Password show/hide
actions include Lucide eye icons alongside their existing labels.
`I18nextProvider` owns React translation context; account messages retain their
server source and workspace translations use separate English/Khmer JSON resources.
The library change does not change the product's locale or iconography policy.

Linked-account failures reuse the shared AccountFeedback/Notice alert, with one
English/Khmer message per failed attempt. The page-level generic fallback appears
only when no server-owned error key is available, avoiding duplicate alerts.

## Management workflows

Deactivation requests have their own permission-gated Management navigation item.
Review uses the shared table, ActionForm, fields and confirmation dialog. Pending,
approved and rejected states use localized text, with required review notes and a
visible decision date. The owner's request page also shows rejection notes.

Audit filters use shared fields and native date controls, with explicit UTC labels.
Repeated failed sign-ins are described as a review indicator. Admin overview lists
system activities with actor names when audit access is available. User-security
tools show current enabled/locked status and only the relevant state-change actions.
Assignment search reuses existing list APIs, selects into the validated UUID field,
and retains manual UUID entry for administrators without list permissions.
