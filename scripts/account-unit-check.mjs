import assert from "node:assert/strict";
import { describeDevice } from "../iam-web/iam-web-reactjs/src/lib/device.js";

for (const [ua, browser, os, kind] of [
    [
        "Mozilla/5.0 (Windows NT 10.0) Chrome/130.0 Safari/537.36 Edg/130.0",
        "Microsoft Edge",
        "Windows",
        "computer",
    ],
    [
        "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) CriOS/130.0 Mobile/15E148 Safari/604.1",
        "Chrome",
        "iOS",
        "mobile",
    ],
    [
        "Mozilla/5.0 (Android 14; Mobile) Firefox/130.0",
        "Firefox",
        "Android",
        "mobile",
    ],
    [
        "Mozilla/5.0 (iPad; CPU OS 18_0 like Mac OS X) Version/18.0 Safari/604.1",
        "Safari",
        "iOS",
        "tablet",
    ],
    [
        "Mozilla/5.0 (Linux) Chrome/130.0 Safari/537.36 OPR/115.0",
        "Opera",
        "Linux",
        "computer",
    ],
    ["unrecognized client", "", "", "unknown"],
    ["", "", "", "unknown"],
])
    assert.deepEqual(describeDevice(ua), { browser, os, kind });
console.log(
    "Device display checks passed (desktop, mobile, tablet and unknown clients).",
);

// Account navigation refreshes server-projected values and CSRF; workspace filters stay local.
const { shouldReloadPage } =
    await import("../iam-web/iam-web-reactjs/src/lib/page-loader.js");
for (const [from, to, expected] of [
    ["/workspace?panel=overview", "/profile/edit", true],
    ["/profile/edit", "/profile/apps", true],
    ["/login?lang=km", "/login?lang=en", true],
    ["/workspace?panel=users", "/workspace?panel=roles", false],
    ["/workspace?lang=en&panel=users", "/workspace?lang=km&panel=users", true],
    ["/profile/edit", "/profile/edit", true],
])
    assert.equal(
        shouldReloadPage({
            currentUrl: new URL(from, "https://iam.example"),
            nextUrl: new URL(to, "https://iam.example"),
            defaultShouldRevalidate: false,
        }),
        expected,
        `${from} -> ${to}`,
    );
console.log("Router revalidation checks passed.");
