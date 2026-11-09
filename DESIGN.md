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

Runtime ownership: `iam-web/src/main/resources/static/css/login.css` owns primitive
color/type tokens; `account.css` adapts them into shared controls and layout. This
file documents those accepted tokens; it does not independently generate CSS.

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

Account content max-width 1120px; authentication max-width 600px. Profile sections
use two columns above 700px and one below. Long forms use document scrolling;
no viewport-height container clips them. Header/account navigation remains reachable
on narrow screens. Controls are at least 44px high, and error regions reserve space.

## Elevation & Depth

Borders and spacing express hierarchy. Cards have white surfaces without ambient
blur or decorative shadows. Only the unsaved-changes dialog uses a backdrop.

## Shapes

Controls use 8px radius, cards 16px radius. Do not change these per screen.

## Components

Shared JTE layout, field, CSRF, and notice templates own recurring behavior.
Primary buttons use cobalt; secondary actions use white; final destructive actions
use danger styling. Busy buttons retain dimensions and have `aria-busy` on forms.
Inputs use visible labels, error descriptions, appropriate autofill and password
masking with an accessible show/hide action. Success/error notices use status/alert.

Native select and date controls are intentional; platform popup geometry is accepted.
Unlinking and consent revocation use separate confirmation pages with object names,
consequences and a safe cancel destination. Dirty in-app navigation uses one shared
app-owned dialog. Actual tab closure uses the narrow beforeunload mechanism.

### Iconography

Text labels carry meaning. Provider names are localized strings, not unlabeled icons.

### Motion

No ambient animation. Reduced-motion users get immediate state changes.

### Content and data visualization

All visible labels and feedback come from message bundles. English and Khmer have
matching keys. Usernames, client names, scopes and claims are data, not translation
keys. Do not render passwords, tokens, raw provider snapshots, or database errors.

## Do's and Don'ts

- Reuse the shared field/notice/layout templates for every account page.
- Preserve non-secret input on validation failure, and focus the first error.
- Keep consent and permissions explicit; do not infer account ownership from email.
- Do not add native alert/confirm/prompt calls or bypass browser CSRF protection.
