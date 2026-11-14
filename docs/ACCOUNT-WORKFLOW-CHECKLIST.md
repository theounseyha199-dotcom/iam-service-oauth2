# Account workflow evidence (+40-point rubric)

| Requirement | Implementation | Verification |
| --- | --- | --- |
| Profile (+5) | `/profile/edit`: saved summary, editable name/username/email/phone/picture URL, live preview and initials fallback; field-specific duplicate errors | Browser save/reload, unsafe/broken image fallback, narrow viewport; duplicate username/email integration test |
| Security & Password (+5) | `/profile/password`, `/forgot-password`, `/reset-password`: current-password validation, single-use expiring recovery and CSRF | Password/recovery integration tests; browser validation and password visibility |
| Linked Accounts (+5) | `/profile/accounts`: connected status, available-provider actions, explicit disconnect confirmation, last-method protection | Identity ownership/link/unlink integration tests and browser account navigation; live external-provider sign-in needs its configured credentials/callbacks |
| Active Sessions (+5) | `/?panel=sessions`: browser, OS/device type, IP, approximate/local/unavailable location, current-device marker and last activity | Device parser checks, offline GeoIP fixture tests, browser session table and Khmer mobile navigation |
| Session Management (+5) | Individual termination/all devices; expired workspace API requests return 401 | Two clients for the same user plus another user; selected-device termination, owner isolation and all-device revocation |
| Login History (+5) | `/?panel=history`: success/failure filter, date/time, IP and readable device details | Real failed sign-in integration test; browser history table, paging/filter implementation |
| Account Activity (+5) | `/?panel=activity`: named account/security events and outcomes, reset/link completion events | Failed profile-update event and successful reset event integration tests; browser error/retry recovery |
| Deactivation (+5) | `/?panel=deactivation`: confirmation, pending-review status/time/reason, reload persistence and request update | Own-request access isolation, one-record upsert and account-remains-active integration tests; browser submit/update/reload |

## Commands

```sh
./gradlew check -Ppart2Integration=true -PbrowserChecks=true
./gradlew :iam-main:test --tests co.istad.iam.SessionLocationTest
node scripts/account-unit-check.mjs
```

Frontend build/lint run as part of Gradle verification. Integration/browser tests
use disposable databases and synthetic accounts. Generated screenshots are under
`verification/browser`; static UI audit is `verification/account-ui-audit.json`.

## Configuration limits

Pictures use HTTP(S) URLs, with no upload service. Public-IP city/country lookup
requires `IAM_GEOIP_DATABASE` pointing to a real GeoIP2/GeoLite2 City database;
localhost correctly displays Local network. MaxMind's bundled synthetic test file
is only a test fixture. Session enumeration covers this server process. Password
recovery requires mail delivery; external social sign-in requires provider accounts
and callback credentials. Deactivation requests await administrative review and do
not imply an automatic account-disable or approval workflow.
