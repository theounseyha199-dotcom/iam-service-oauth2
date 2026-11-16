this# IAM dashboard demo script

Use this for a live presentation or recording. Allow approximately 15–20 minutes;
rehearse once before presenting. The sequence follows Assignment IV: Account A1–A8,
then Management M1–M12. The separate video-format marking criteria are outside this script.

## Prepare before the demo

- Start `IamServiceApplication` in IntelliJ with the corrected private run configuration.
- Confirm the database is `jdbc:postgresql://localhost:9543/db_oauth2` and the active mail profile is `gmail`.
- Use two separate browser profiles or two different browsers so sessions are independent.
  Two ordinary tabs in the same profile usually share one session.
- Keep the administrator, `theounseyha`, in browser A. Use the password configured
  in `IAM_ADMIN_PASSWORD`; keep the password field masked.
- Prepare `demo-user` as a normal USER with an email inbox you control. Verify its
  email using the actual OTP from Gmail before starting the main presentation.
- Prepare a disposable user for the user-deletion example. Keep the administrator
  account available throughout the demo.
- Prepare two custom roles, `DemoSupport` and `DemoReviewer`, and a custom permission,
  `demo:read`, for the assignment examples. Leave the normal user's USER role intact.
- Use a reachable profile-picture URL and have the mailbox ready in a separate window.
- Rehearse Google linking in a regular browser. Provider consent, callback approval,
  and MFA must work before you describe that flow as successful.
- For the IP demonstration, use the laptop's current LAN URL instead of `localhost`.
  For example, if its current Wi-Fi address is `192.168.1.15`, open
  `http://192.168.1.15:9000` and sign in there. This example address is not hardcoded
  in the application. A second device on the same Wi-Fi should use that URL too.
- Local sign-in can be demonstrated over the LAN URL. For social login, use the
  origin that matches the provider's approved callback; switching hosts changes cookies
  and can invalidate an OAuth callback.
- Close environment files, private run settings, database dumps, and unrelated accounts.

## Opening — about 30 seconds

**Show:** the login page and then the dashboard.

**Say:**

> Hello, my name is [your name]. This is my IAM dashboard built with React and a
> Spring Boot IAM backend. It provides account management, authentication, roles,
> permissions, OAuth clients, and security administration. The interface supports
> English and Khmer. I will demonstrate the Account features first, then Management.

## Account — approximately 6–8 minutes

### A1. Profile (+5)

**Do:** In browser B, sign in as `demo-user`. Open **Personal information**. Show
the saved name, username, email, phone number, and picture. Change the name, phone,
and picture URL; save and reload. Point out that username and email are editable too.
Change the email only if you have time to complete any required verification.

**Say:**

> Users can view and update their personal information here. I will save these
> changes and reload the page to show that they are stored in PostgreSQL.

### A2. Security & Password (+5)

**Do:** Open **Security & password**. Enter a wrong current password once and show
the validation. Change the password correctly. Then sign out, use **Forgot password**,
open the actual reset email, and set another password. Sign in with the new password.
Keep credentials and the reset-link token out of the presentation.

**Say:**

> Changing a password requires the current password. Password recovery sends an
> expiring, single-use link through Spring JavaMailSender and Gmail SMTP. The reset
> revokes stored OAuth authorizations and tracked browser sessions. Previously issued
> JWT access tokens retain their existing expiry behavior.

**If explaining OTP:**

> Registration verification uses a six-digit code sent by email. Redis stores a
> salted hash with a five-minute expiry and a bounded attempt limit. The code can
> be consumed once.

### A3. Linked Account (+5)

**Do:** Open **Linked accounts**. Select Google, complete the real provider flow,
and return to IAM. Show the connected status. Choose unlink and confirm. Use an
account that has a working password so it retains another sign-in method.

**Say after a successful flow:**

> The user can link an external provider to the current account and disconnect it
> later. The provider identity is associated with this account explicitly; matching
> email addresses do not silently merge accounts.

**If the provider blocks the flow, say instead:**

> The application reaches the provider, but live consent is blocked in this
> environment. I cannot claim successful end-to-end linking from this attempt.

Do not present automated-browser rejection as a successful social-login demonstration.

### A4. Active Sessions (+5)

**Do:** Sign `demo-user` in from a second browser profile or another device. Open
**Active sessions** in browser B. Show browser, operating system/device, IP address,
location state, last activity, and the current-session indicator.

**Say:**

> The application records session information from real requests. Localhost requests
> appear as 127.0.0.1 or ::1. Requests through the LAN address can show the client's
> private network address. The system does not replace a client's address with the
> server's Wi-Fi address.

**Location explanation:**

> Private network addresses display Local network. Public-IP city and country lookup
> requires a real GeoIP dataset. This environment currently has no configured dataset,
> so I will not claim a real public-IP location demonstration.

### A5. Session Management (+5)

**Do:** Terminate the secondary session. Refresh its protected page and show that
it requires sign-in again. Then demonstrate **Terminate all sessions** and sign
back in before continuing.

**Say:**

> Users can end a selected session or all tracked browser sessions. I will refresh
> the other browser to show that its session has actually expired.

### A6. Login History (+5)

**Do:** From a signed-out browser, submit one incorrect password for `demo-user`.
Return to the signed-in account's **Login history**. Show successful and failed
attempts, timestamps, device information, and the outcome filter.

**Say:**

> Login history records both successful and failed attempts. Users can review when
> an attempt happened and the available device and network information.

### A7. Account Activity (+5)

**Do:** Open **Account activity**. Find the profile update and password-related
events generated earlier. Show their times and outcomes.

**Say:**

> Account activity provides a security history beyond sign-ins. These entries come
> from actions performed in the application, such as profile and password changes.

### A8. Account Deactivation (+5)

**Do:** Open **Request deactivation**. Enter `Demo account closure request`, confirm,
and show the pending status. Reload to show that the request persists. Leave the
request pending until M10.

**Say:**

> A user can request deactivation with a reason. Submitting a request does not
> immediately disable the account; an administrator reviews it separately.

## Management — approximately 7–10 minutes

Switch to browser A, signed in as `theounseyha` with the ADMIN role.

### M1. Admin Overview (+2.5)

**Do:** Show total users, enabled users, roles, permissions, clients, and recent
system activities. Show the pending deactivation queue. Its status dropdown is
available on the dedicated **Deactivation requests** page, not on the overview.

**Say:**

> The administrator overview summarizes account and access-management data. It also
> shows recent system activities and pending requests that need review.

### M2. User (+2.5)

**Do:** Open **Users**. Create a disposable user, view its details, edit its name,
disable and enable it, lock and unlock it, then delete it with confirmation.
Use `demo-user` for the remaining demonstrations, not this deleted account.

**Say:**

> Administrators can create, view, update, deactivate, reactivate, lock, unlock, and
> delete users. The dashboard uses soft deletion to preserve application history.

### M3. User Role Assignment (+2.5)

**Do:** Open `demo-user` and its access controls. Assign `DemoSupport` with a reason.
Replace it by revoking that assignment and assigning `DemoReviewer`. Revoke
`DemoReviewer` afterward. Show the resulting effective roles.

**Say:**

> Roles can be assigned and revoked with a reason. To change this user's additional
> role, I revoke the old assignment and add the replacement. Assignment history is retained.

### M4. User Permission Assignment (+2.5)

**Do:** Grant `demo:read` directly to `demo-user`. Show it in direct and effective
permissions. Revoke it and refresh the display.

**Say:**

> Permissions can also be granted directly to a user. This is separate from role-based
> access. I will revoke this direct grant and show the updated effective permissions.

### M5. User Sessions (+5)

**Do:** Keep `demo-user` signed in on two independent browser sessions. In its admin
details, open **Sessions**. End one session and verify it from that browser. Sign
it back in, then end all of that user's sessions and verify again.

**Say:**

> Authorized administrators can inspect another user's tracked sessions and terminate
> one or all of them. These operations are checked on the server.

### M6. User Login History (+5)

**Do:** Open the user's **Login history**. Filter failed attempts and show device/time
details. If repeated failures were prepared, show the suspicious-activity indicator.

**Say:**

> Administrators can inspect successful and failed sign-ins. The repeated-failure
> indicator helps identify attempts that need review; it is not proof of an attack.

### M7. Role (+5)

**Do:** Open **Roles**. Create `DemoEditor`, view it, edit its description, assign
`demo:read`, remove that permission, then delete this custom role.

**Say:**

> Role management includes creating, viewing, updating, and deleting custom roles,
> plus adding and removing their permissions. System roles are protected.

### M8. Permission (+5)

**Do:** Open **Permissions**. Create a custom permission with resource `demo` and
action `export`. Show its name, `demo:export`, view it, update its description,
and delete it.

**Say:**

> Permissions identify a resource and an action. This creates demo:export, which can
> be assigned through roles or direct user grants. Custom permissions support the
> demonstrated create, read, update, and delete workflow.

### M9. Client (+5)

**Do:** Open **OAuth2 clients** and create a temporary client:

| Field | Demo value |
| --- | --- |
| Client ID | `iam-demo-client` |
| Application name | `IAM Demo Client` |
| Confidential | Enabled |
| Client secret | Click Generate secret, then Copy secret; keep the field masked |
| Grant types | Check Authorization Code and Refresh Token |
| Redirect URIs | `http://localhost:3000/callback` |
| Scopes | Check `openid`, `profile`, and `email` |
| Access-token lifetime | 5 minutes |
| Refresh-token lifetime | 60 minutes |

View the saved client, change its name, save again, and delete the temporary client.
Optionally demonstrate **Add custom scope** with `demo:read` and show that its
checkbox remains selected when you reopen the application for editing.

**Say:**

> Administrators can register and manage OAuth2/OIDC clients, including credentials,
> grant types, redirect URIs, scopes, and token lifetimes. Configuring this example
> does not itself demonstrate a complete external application's OAuth integration.

### M10. Account Security Management (+5)

**Do:** Reset `demo-user`'s password from its admin security controls. Show lock/unlock
controls. Then open **Deactivation requests**, approve the pending request from A8
with a reason, and show that the user is disabled. Attempt a protected action in
the user's browser. Restore access afterward only if the account is needed again.

**Say:**

> Administrators can reset passwords, unlock accounts, and manage account status.
> Approving this deactivation request disables the account and ends its tracked
> sessions. An administrator cannot review their own request.

### M11. Audit Logs (+5)

**Do:** Open **Audit logs**. Locate events generated by the user changes, role and
permission assignments, and deactivation review. Demonstrate event/outcome filters
and at least one actor, target, or date filter. Clear the filters afterward.

**Say:**

> Audit logs provide a system-level view of who performed an action, its target,
> outcome, and time. Filters help administrators investigate specific activities.

### M12. System Configuration (+5)

**Do:** Open **System configuration**. Show consent, password minimum length,
number/uppercase requirements, idle-session timeout, and token defaults. Record
the original values, save a small change, reload to prove persistence, then restore
the original values.

**Say:**

> Administrators can configure the available authentication and password policies,
> session expiration, and token defaults. Password rules apply when passwords are
> created or changed. The session timeout applies on a subsequent request. Token
> defaults apply to newly registered clients; existing client lifetimes are edited
> on their individual client records.

## English/Khmer, mobile, and permissions — about 1 minute

**Do:** Switch the interface to Khmer. Open a representative account page and an
admin page. Use a narrow viewport and open the navigation drawer. Return to the
normal USER account to show that administrative navigation is unavailable. Restore
its enabled state and sign it in again first if M10 disabled it.

**Say:**

> English and Khmer are available throughout the interface. The layout adapts to
> smaller screens. Administrative actions require server-side permissions; hiding
> navigation is only the user-interface layer of access control.

## Closing — about 20 seconds

> This completes the Account and Management demonstration. The project uses React,
> Spring Boot, PostgreSQL, Redis verification codes, and JavaMailSender. Automated
> checks cover the core security and dashboard workflows. Live provider consent and
> public-IP geolocation are only claimed where they were actually demonstrated.
> Thank you.

## Final rehearsal checklist

- Every save or delete includes its visible result, not just the form submission.
- OTP/reset messages reach the actual mailbox before you begin.
- Social linking is rehearsed in a regular browser with an approved callback.
- Session termination is verified from a second independent session.
- The IP explanation distinguishes localhost, LAN, and public addresses accurately.
- Neither an environment file nor a real password/token is displayed.
- Temporary records are cleaned up, and policy values are restored.
- The administrator remains enabled and usable at the end.
