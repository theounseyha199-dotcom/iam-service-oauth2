export const pageRoutes = [
    ["", "WorkspacePage"],
    ["workspace", "WorkspacePage"],
    ["login", "LoginPage"],
    ["register", "RegisterPage"],
    ["profile/edit", "ProfileEditPage"],
    ["profile/password", "ChangePasswordPage"],
    ["profile/accounts", "LinkedAccountsPage"],
    ["profile/accounts/:id/unlink", "UnlinkAccountPage"],
    ["profile/apps", "ConnectedAppsPage"],
    ["profile/apps/:id/revoke", "RevokeAppPage"],
    ["oauth2/consent", "ConsentPage"],
    ["logout", "LogoutPage"],
    ["federation/complete", "FederationCompletePage"],
    ["forgot-password", "ForgotPasswordPage"],
    ["reset-password", "ResetPasswordPage"],
    ["registration-pending", "VerifyEmailPage"],
    ["verify-email", "VerifyEmailPage"],
    ["resend-verification", "RegistrationPendingPage"],
];
const bootstrapElement = document.getElementById("iam-page");
const contextPath = bootstrapElement
    ? JSON.parse(bootstrapElement.textContent).contextPath
    : "";
export function isAppPath(pathname) {
    if (contextPath && !pathname.startsWith(contextPath + "/")) return false;
    const local = pathname
        .slice(contextPath.length)
        .replace(/^\//, "")
        .replace(/\/$/, "");
    return pageRoutes.some(([path]) =>
        new RegExp("^" + path.replace(/:[^/]+/g, "[^/]+") + "$").test(local),
    );
}
