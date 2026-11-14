import {
    chromium,
    expect,
} from "../iam-web/iam-web-reactjs/node_modules/@playwright/test/index.mjs";
import { mkdir } from "node:fs/promises";
const base = process.env.IAM_BROWSER_URL;
const password = process.env.IAM_BROWSER_PASSWORD;
const browser = await chromium.launch({ headless: true });
const errors = [];
await mkdir("verification/browser", { recursive: true });
async function login(page, username) {
    page.on("pageerror", (error) => errors.push(error.message));
    await page.goto(base + "/login?lang=en");
    await page.locator("#username").fill(username);
    await page.locator("#password").fill(password);
    await page.getByRole("button", { name: "Sign in", exact: true }).click();
    await expect(
        page.getByRole("heading", { name: "Overview", exact: true }),
    ).toBeVisible();
}
try {
    const page = await browser.newPage({
        viewport: { width: 1440, height: 1000 },
    });
    await login(page, process.env.IAM_BROWSER_USERNAME);
    const desktopNavigation = page.getByRole("button", {
        name: "Toggle navigation",
        exact: true,
    });
    await desktopNavigation.click();
    await expect(
        page.locator('[data-slot="sidebar"][data-state]'),
    ).toHaveAttribute("data-state", "collapsed");
    await desktopNavigation.click();
    await expect(
        page.locator('[data-slot="sidebar"][data-state]'),
    ).toHaveAttribute("data-state", "expanded");

    const management = page.getByRole("navigation", {
        name: "Management",
        exact: true,
    });
    await expect(
        management.getByRole("link", { name: "Users", exact: true }),
    ).toBeVisible();
    await expect(page.locator(".workspace-stats .card")).toHaveCount(5);
    await expect(page.locator(".workspace-state")).toHaveCount(0);
    // Wait for the sidebar expansion animation before measuring route stability.
    await expect(page.locator('[data-slot="sidebar-container"]')).toHaveCSS(
        "width",
        "256px",
    );
    await page.screenshot({
        path: "verification/browser/workspace-overview.png",
        fullPage: true,
    });
    const shell = page.locator('[data-slot="sidebar-wrapper"]');
    const sidebarWidth = (
        await page.locator('[data-slot="sidebar-container"]').boundingBox()
    ).width;
    const routeMarker = await page.evaluate(() => {
        window.__iamRouteMarker = crypto.randomUUID();
        return window.__iamRouteMarker;
    });
    for (const [label, field] of [
        ["Personal information", "#username"],
        ["Security & password", "#currentPassword"],
        ["Linked accounts", null],
        ["Connected apps", null],
    ]) {
        await page
            .getByRole("navigation", { name: "My account", exact: true })
            .getByRole("link", { name: label, exact: true })
            .click();
        await expect(shell).toHaveCount(1);
        await expect(page.locator(".dashboard-header")).toHaveCount(1);
        await expect(page.locator(".site-header,.account-nav")).toHaveCount(0);
        await expect(
            management.getByRole("link", { name: "Users", exact: true }),
        ).toBeVisible();
        await expect(
            page
                .getByRole("navigation", { name: "My account", exact: true })
                .getByRole("link", { name: label, exact: true }),
        ).toHaveAttribute("aria-current", "page");
        if (field) await expect(page.locator(field)).toBeVisible();
        await expect
            .poll(() => page.evaluate(() => window.__iamRouteMarker))
            .toBe(routeMarker);
        await expect(page.locator('[data-slot="sidebar-container"]')).toHaveCSS(
            "width",
            `${sidebarWidth}px`,
        );
    }
    await page.goBack();
    await expect(page).toHaveURL(/\/profile\/accounts/);
    await expect(
        page.getByRole("heading", { name: "Linked accounts", exact: true }),
    ).toBeVisible();
    await page.goForward();
    await expect(page).toHaveURL(/\/profile\/apps/);
    await page
        .getByRole("navigation", { name: "My account", exact: true })
        .getByRole("link", { name: "Personal information", exact: true })
        .click();
    await page.locator("#nickname").fill("Unsaved router draft");
    await page.goBack();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page
        .getByRole("button", { name: "Keep editing", exact: true })
        .click();
    await expect(page.locator("#nickname")).toHaveValue("Unsaved router draft");
    await page.goBack();
    await page.getByRole("button", { name: "Leave page", exact: true }).click();
    await expect(page).toHaveURL(/\/profile\/apps/);
    await expect
        .poll(() => page.evaluate(() => window.__iamRouteMarker))
        .toBe(routeMarker);
    await page.goto(base + "/profile/edit?lang=en");
    await page.reload();
    await expect(shell).toHaveCount(1);
    await expect(
        management.getByRole("link", { name: "Users", exact: true }),
    ).toBeVisible();
    await page.route("https://images.example.test/**", (route) =>
        route.fulfill({
            contentType: "image/svg+xml",
            body: '<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64"><rect width="64" height="64" fill="#364fc7"/></svg>',
        }),
    );
    await page.locator("#pictureUrl").fill("javascript:alert(1)");
    await expect(
        page.getByRole("img", { name: "Picture preview", exact: true }),
    ).toHaveCount(0);
    await page
        .locator("#pictureUrl")
        .fill("https://images.example.test/profile.svg");
    await expect(
        page.getByRole("img", { name: "Picture preview", exact: true }),
    ).toBeVisible();
    await page.locator("#phoneNumber").fill("+85512345678");
    await page
        .getByRole("button", { name: "Save changes", exact: true })
        .click();
    await expect(page.getByRole("status")).toContainText("saved");
    await expect(
        page.getByRole("region", { name: "Current profile", exact: true }),
    ).toContainText("+85512345678");
    await expect(
        page.getByRole("img", { name: "Profile picture", exact: true }),
    ).toBeVisible();
    await page.reload();
    await expect(page.locator("#pictureUrl")).toHaveValue(
        "https://images.example.test/profile.svg",
    );
    await page.unroute("https://images.example.test/**");
    await page.route("https://images.example.test/**", (route) =>
        route.abort(),
    );
    await page
        .locator("#pictureUrl")
        .fill("https://images.example.test/unavailable.svg");
    await expect(
        page.getByRole("img", { name: "Picture preview", exact: true }),
    ).toHaveCount(0);
    await page.locator("#pictureUrl").fill("");
    await page
        .getByRole("button", { name: "Save changes", exact: true })
        .click();
    await expect(page.getByRole("status")).toContainText("saved");
    await page.screenshot({
        path: "verification/browser/unified-profile-layout.png",
        fullPage: true,
    });
    await page.setViewportSize({ width: 390, height: 844 });
    if (
        await page.evaluate(
            () => document.documentElement.scrollWidth > innerWidth,
        )
    )
        throw new Error("Profile mobile overflow");
    await page.screenshot({
        path: "verification/browser/account-profile-mobile.png",
        fullPage: true,
    });
    await page.setViewportSize({ width: 1440, height: 1000 });
    await page
        .getByRole("navigation", { name: "My account", exact: true })
        .getByRole("link", { name: "Overview", exact: true })
        .click();
    await expect(page).toHaveURL(/\/workspace\?panel=overview/);
    await expect(
        page.getByRole("heading", { name: "Overview", exact: true }),
    ).toBeVisible();
    await management.getByRole("link", { name: "Users", exact: true }).click();
    await expect(page.getByRole("table")).toBeVisible();
    await page
        .getByRole("button", { name: "Create · Users", exact: true })
        .click();
    await page
        .getByRole("button", { name: "Save changes", exact: true })
        .click();
    await expect(page.locator("#w-username")).toBeFocused();
    const name = "browser" + Date.now();
    await page.locator("#w-username").fill(name);
    await page.locator("#w-email").fill(name + "@example.com");
    await page.locator("#w-givenName").fill("Workspace");
    await page.locator("#w-familyName").fill("Browser");
    await page.locator("#w-password").fill(password);
    await page
        .getByRole("button", { name: "Save changes", exact: true })
        .click();
    await expect(
        page.getByRole("button", { name: "Create · Users", exact: true }),
    ).toBeVisible();
    await page.locator("#workspace-search").fill(name);
    await expect(page).toHaveURL(new RegExp("search=" + name));
    await expect(page.getByRole("row").filter({ hasText: name })).toHaveCount(
        1,
    );
    await page
        .getByRole("row")
        .filter({ hasText: name })
        .getByRole("button", { name: "Edit", exact: true })
        .click();
    await expect(page.locator("#w-email")).toHaveValue(name + "@example.com");
    await page.locator("#w-givenName").fill("Edited browser");
    await page
        .getByRole("button", { name: "Save changes", exact: true })
        .click();
    await expect(page.getByRole("row").filter({ hasText: name })).toContainText(
        "Edited browser",
    );
    // React Hook Form + Zod validates password matching and clears its own state after reset.
    await page
        .getByRole("row")
        .filter({ hasText: name })
        .getByRole("button", { name: "Edit", exact: true })
        .click();
    const roleAssignment = page
        .getByRole("heading", { name: "User roles", exact: true })
        .locator("..");
    await roleAssignment.getByLabel("Find a role or permission").fill("USER");
    await roleAssignment
        .getByRole("button", { name: "Search", exact: true })
        .click();
    const userRole = roleAssignment
        .locator("select[id$='-record'] option")
        .filter({ hasText: /^USER ·/ });
    await expect(userRole).toHaveCount(1);
    const userRoleId = await userRole.getAttribute("value");
    await roleAssignment
        .getByLabel("Select a record", { exact: true })
        .selectOption(userRoleId);
    await expect(page.locator("#w-roleId")).toHaveValue(userRoleId);
    await page.locator("#w-user-roles-reason").fill("Browser role assignment");
    await roleAssignment
        .getByRole("button", { name: "Assign", exact: true })
        .click();
    await page
        .getByRole("dialog")
        .getByRole("button", { name: "Assign", exact: true })
        .click();
    await expect(
        page.getByRole("region", { name: "Roles", exact: true }),
    ).toContainText("USER");
    await roleAssignment
        .getByLabel("Action", { exact: true })
        .selectOption("revoke");
    await roleAssignment
        .getByRole("button", { name: "Revoke", exact: true })
        .click();
    await page
        .getByRole("dialog")
        .getByRole("button", { name: "Revoke", exact: true })
        .click();
    await expect(
        page.getByRole("region", { name: "Roles", exact: true }),
    ).toContainText("No records found.");
    await page.locator("#w-newPassword").fill("NewBrowserPassword2026!");
    await page.locator("#w-confirmPassword").fill("different");
    await page
        .getByRole("button", { name: "Reset password", exact: true })
        .click();
    await expect(page.locator("#w-confirmPassword")).toHaveAttribute(
        "aria-invalid",
        "true",
    );
    await expect(page.getByRole("dialog")).not.toBeVisible();
    await page.locator("#w-confirmPassword").fill("NewBrowserPassword2026!");
    await page
        .getByRole("button", { name: "Reset password", exact: true })
        .click();
    await page
        .getByRole("dialog")
        .getByRole("button", { name: "Reset password", exact: true })
        .click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
    await expect(page.locator("#w-newPassword")).toHaveValue("");
    await expect(page.locator("#w-confirmPassword")).toHaveValue("");
    await page
        .getByRole("button", { name: "Reset password", exact: true })
        .click();
    await expect(page.locator("#w-newPassword")).toBeFocused();
    await expect(page.getByRole("dialog")).not.toBeVisible();
    await page
        .getByRole("button", { name: "Back to list", exact: true })
        .click();
    await expect(page.getByRole("row").filter({ hasText: name })).toBeVisible();
    await page
        .getByRole("row")
        .filter({ hasText: name })
        .getByRole("button", { name: "Delete", exact: true })
        .click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await expect(
        page
            .getByRole("dialog")
            .getByRole("button", { name: "Cancel", exact: true }),
    ).toBeFocused();
    await page
        .getByRole("dialog")
        .getByRole("button", { name: "Cancel", exact: true })
        .click();
    await expect(page.getByRole("row").filter({ hasText: name })).toBeVisible();
    await page
        .getByRole("row")
        .filter({ hasText: name })
        .getByRole("button", { name: "Delete", exact: true })
        .click();
    await page
        .getByRole("dialog")
        .getByRole("button", { name: "Delete", exact: true })
        .click();
    await expect(
        page.getByText("No records found.", { exact: true }),
    ).toBeVisible();
    await page
        .getByRole("button", { name: "Clear search", exact: true })
        .click();
    await expect(page.getByRole("table")).toContainText(
        process.env.IAM_BROWSER_USERNAME,
    );
    await management.getByRole("link", { name: "Roles", exact: true }).click();
    await page
        .getByRole("button", { name: "Create · Roles", exact: true })
        .click();
    await page.locator("#w-name").fill("role" + name);
    await page.locator("#w-description").fill("Browser role");
    await page
        .getByRole("button", { name: "Save changes", exact: true })
        .click();
    await expect(page.getByRole("table")).toContainText("role" + name);
    await management
        .getByRole("link", { name: "OAuth2 clients", exact: true })
        .click();
    await page
        .getByRole("button", { name: "Create · OAuth2 clients", exact: true })
        .click();
    await page.locator("#w-clientId").fill("client" + name);
    await page.locator("#w-clientName").fill("Browser test client");
    await page
        .locator("#w-clientSecret")
        .fill("browser-client-secret-123456789");
    await page.locator("#w-redirectUris").fill("https://example.com/callback");
    await page
        .getByRole("button", { name: "Save changes", exact: true })
        .click();
    await expect(page.getByRole("table")).toContainText("client" + name);
    await expect(page.getByRole("table")).not.toContainText(
        "browser-client-secret",
    );
    await management
        .getByRole("link", { name: "Audit logs", exact: true })
        .click();
    await expect(page.getByRole("table")).toContainText("OAuth client changed");
    await page.locator("#w-actor").fill("invalid");
    await page
        .getByRole("button", { name: "Apply filters", exact: true })
        .click();
    await expect(page.locator("#w-actor")).toHaveAttribute(
        "aria-invalid",
        "true",
    );
    await page.locator("#w-actor").fill(process.env.IAM_BROWSER_ADMIN_UUID);
    await page.locator("#w-target").fill("/clients");
    const filteredActivity = page.waitForResponse(
        (response) =>
            response.url().includes("/workspace/api/activity?") &&
            new URL(response.url()).searchParams.get("target") === "/clients",
    );
    await page
        .getByRole("button", { name: "Apply filters", exact: true })
        .click();
    await filteredActivity;
    await expect(page).toHaveURL(/target=%2Fclients/);
    await expect(page.getByRole("table")).toContainText("OAuth client changed");
    await page.locator("#w-from").fill("2099-01-01");
    await expect(page.locator("#w-from")).toHaveValue("2099-01-01");
    await page
        .getByRole("button", { name: "Apply filters", exact: true })
        .click();
    await expect(page).toHaveURL(/from=2099-01-01/);
    await expect(page.getByRole("table")).toContainText("No records found.");
    await page
        .getByRole("button", { name: "Clear filters", exact: true })
        .click();
    await expect(page.locator("#w-from")).toHaveValue("");
    await management
        .getByRole("link", { name: "System configuration", exact: true })
        .click();
    await expect(page.locator("#w-sessionMinutes")).toBeVisible();
    await page.locator("#w-sessionMinutes").fill("35");
    await page
        .getByRole("button", { name: "Save changes", exact: true })
        .click();
    await page
        .getByRole("dialog")
        .getByRole("button", { name: "Save changes", exact: true })
        .click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
    await page.reload();
    await expect(page.locator("#w-sessionMinutes")).toHaveValue("35");
    await page.goto(base + "/?panel=sessions&lang=en");
    await expect(page.getByRole("table")).toContainText("This device");
    await expect(page.getByRole("table")).toContainText("Chrome");
    await expect(page.getByRole("table")).toContainText("Local network");
    await expect(page.getByRole("table")).not.toContainText("Mozilla/5.0");
    await page.screenshot({
        path: "verification/browser/workspace-sessions.png",
        fullPage: true,
    });
    await page.emulateMedia({ reducedMotion: "reduce" });
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto(base + "/?panel=sessions&lang=km");
    await expect(page.locator("html")).toHaveAttribute("lang", "km");
    await expect(
        page.getByRole("heading", { name: "សម័យចូលប្រើសកម្ម", exact: true }),
    ).toBeVisible();
    const mobileNavigation = page.getByRole("button", {
        name: "បើក ឬបិទម៉ឺនុយរុករក",
        exact: true,
    });
    await mobileNavigation.click();
    await expect(
        page.getByRole("dialog", { name: "ម៉ឺនុយរុករក", exact: true }),
    ).toBeVisible();
    await page.screenshot({
        path: "verification/browser/shadcn-mobile-drawer.png",
        fullPage: true,
    });
    await page.keyboard.press("Escape");
    await expect(page.getByRole("dialog")).not.toBeVisible();
    await expect(mobileNavigation).toBeFocused();
    await mobileNavigation.click();
    await page
        .getByRole("dialog")
        .getByRole("link", { name: "សម័យចូលប្រើសកម្ម", exact: true })
        .click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
    await expect(page.getByRole("table")).toBeVisible();
    await page.screenshot({
        path: "verification/browser/workspace-khmer-mobile.png",
        fullPage: true,
    });
    if (
        await page.evaluate(
            () => document.documentElement.scrollWidth > innerWidth,
        )
    )
        throw new Error("Mobile workspace overflows");
    await page.locator('a[lang="en"]').click();
    await expect(
        page.getByRole("heading", { name: "Active sessions", exact: true }),
    ).toBeVisible();
    const ordinary = await browser.newPage();
    await login(ordinary, process.env.IAM_BROWSER_USER);
    await expect(
        ordinary.getByRole("navigation", { name: "Management", exact: true }),
    ).toHaveCount(0);
    await ordinary.goto(base + "/?panel=users&lang=en");
    await expect(ordinary.getByRole("alert")).toContainText(
        "You do not have permission",
    );
    await ordinary.goto(base + "/?panel=deactivation&lang=en");
    await ordinary.locator("#w-reason").fill("Browser deactivation request");
    await ordinary
        .getByRole("button", { name: "Request deactivation", exact: true })
        .click();
    await ordinary
        .getByRole("dialog")
        .getByRole("button", { name: "Request deactivation", exact: true })
        .click();
    await expect(ordinary.locator(".workspace-feedback")).toContainText(
        "recorded",
    );
    await expect(
        ordinary.getByRole("heading", {
            name: "Awaiting administrator review",
            exact: true,
        }),
    ).toBeVisible();
    await ordinary.reload();
    await expect(
        ordinary.getByRole("region", {
            name: "Deactivation request status",
            exact: true,
        }),
    ).toContainText("Browser deactivation request");
    await ordinary
        .locator("#w-reason")
        .fill("Updated browser deactivation request");
    await ordinary
        .getByRole("button", { name: "Update request", exact: true })
        .click();
    await expect(ordinary.getByRole("dialog")).toContainText(
        "Your account stays active",
    );
    await ordinary
        .getByRole("dialog")
        .getByRole("button", { name: "Update request", exact: true })
        .click();
    await expect(ordinary.getByRole("dialog")).not.toBeVisible();
    await ordinary.reload();
    await expect(
        ordinary.getByRole("region", {
            name: "Deactivation request status",
            exact: true,
        }),
    ).toContainText("Updated browser deactivation request");
    await ordinary.screenshot({
        path: "verification/browser/account-deactivation.png",
        fullPage: true,
    });
    await page.setViewportSize({ width: 1440, height: 1000 });
    await page.goto(base + "/workspace?panel=reviews&lang=en");
    const requestRow = () =>
        page.getByRole("row").filter({ hasText: process.env.IAM_BROWSER_USER });
    await requestRow()
        .getByRole("button", { name: "Reject", exact: true })
        .click();
    const review = page.getByRole("region", {
        name: "Review deactivation request",
        exact: true,
    });
    await review.getByRole("button", { name: "Reject", exact: true }).click();
    await expect(page.locator("#w-reviewReason")).toBeFocused();
    await expect(page.getByRole("dialog")).not.toBeVisible();
    await page
        .locator("#w-reviewReason")
        .fill("Please confirm before closing your account");
    await review.getByRole("button", { name: "Reject", exact: true }).click();
    await page
        .getByRole("dialog")
        .getByRole("button", { name: "Reject", exact: true })
        .click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
    await expect(requestRow()).toHaveCount(0);
    await ordinary.reload();
    await expect(
        ordinary.getByRole("heading", { name: "Rejected", exact: true }),
    ).toBeVisible();
    await expect(
        ordinary.getByRole("region", {
            name: "Deactivation request status",
            exact: true,
        }),
    ).toContainText("Please confirm before closing your account");
    await ordinary.locator("#w-reason").fill("Confirmed by browser owner");
    await ordinary
        .getByRole("button", { name: "Request deactivation", exact: true })
        .click();
    await ordinary
        .getByRole("dialog")
        .getByRole("button", { name: "Request deactivation", exact: true })
        .click();
    await expect(
        ordinary.getByRole("heading", {
            name: "Awaiting administrator review",
            exact: true,
        }),
    ).toBeVisible();
    await page.reload();
    await expect(requestRow()).toContainText("Confirmed by browser owner");
    await page.screenshot({
        path: "verification/browser/management-deactivation-reviews.png",
        fullPage: true,
    });
    await page.setViewportSize({ width: 390, height: 844 });
    await page.locator('a[lang="km"]').click();
    await expect(
        page.getByRole("heading", { name: "សំណើបិទគណនី", exact: true }).first(),
    ).toBeVisible();
    if (
        await page.evaluate(
            () => document.documentElement.scrollWidth > innerWidth,
        )
    ) {
        throw new Error("Mobile deactivation review overflows");
    }
    await page.screenshot({
        path: "verification/browser/management-reviews-khmer-mobile.png",
        fullPage: true,
    });
    await page.locator('a[lang="en"]').click();
    await expect(requestRow()).toContainText("Confirmed by browser owner");
    await page.setViewportSize({ width: 1440, height: 1000 });
    await ordinary.goto(base + "/?panel=history&lang=en");
    await expect(ordinary.getByRole("table")).toContainText("Successful");
    await ordinary.route("**/workspace/api/activity*", (route) =>
        route.abort(),
    );
    await ordinary.goto(base + "/?panel=activity&lang=en");
    await expect(ordinary.getByRole("alert")).toContainText(
        "could not be completed",
    );
    await expect(
        ordinary.getByRole("button", { name: "Try again", exact: true }),
    ).toBeVisible();
    await ordinary.unroute("**/workspace/api/activity*");
    await ordinary
        .getByRole("button", { name: "Try again", exact: true })
        .click();
    await expect(ordinary.getByRole("table")).toBeVisible();
    await ordinary.goto(base + "/?panel=sessions&lang=en");
    await ordinary
        .getByRole("button", { name: "Log out from all devices", exact: true })
        .click();
    await ordinary
        .getByRole("dialog")
        .getByRole("button", { name: "Log out from all devices", exact: true })
        .click();
    await expect(ordinary).toHaveURL(/\/login/);
    await login(ordinary, process.env.IAM_BROWSER_USER);
    await requestRow()
        .getByRole("button", { name: "Approve", exact: true })
        .click();
    await page
        .locator("#w-reviewReason")
        .fill("Owner confirmed; approved by browser test");
    await review.getByRole("button", { name: "Approve", exact: true }).click();
    await expect(page.getByRole("dialog")).toContainText(
        "ends its active sessions",
    );
    await page
        .getByRole("dialog")
        .getByRole("button", { name: "Approve", exact: true })
        .click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
    await expect(requestRow()).toHaveCount(0);
    await ordinary.goto(base + "/");
    await expect(ordinary).toHaveURL(/\/login/);
    if (errors.length) throw new Error(errors.join("\n"));
    console.log(
        "Workspace browser checks passed: CRUD, confirmations, RBAC, clients, policy, sessions, Khmer mobile, retry, and logout.",
    );
} finally {
    await browser.close();
}
