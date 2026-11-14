export function activityLabel(event, t) {
    const names = {
        profile_updated: "profileUpdated",
        password_changed: "passwordChanged",
        password_reset: "passwordReset",
        social_linked: "socialLinked",
        social_unlinked: "socialUnlinked",
        social_link_started: "socialLinkStarted",
        session_ended: "sessionEnded",
        deactivation_requested: "deactivationRequested",
        deactivation_approved: "deactivationApproved",
        deactivation_rejected: "deactivationRejected",
    };
    if (names[event]) return t(names[event]);
    if (event === "login") return t("login");
    if (event.includes("/password")) return t("passwordChanged");
    if (event.includes("/sessions")) return t("sessionEnded");
    if (event.includes("/deactivation")) return t("deactivationRequested");
    if (event.includes("/profile/edit")) return t("profileUpdated");
    if (event.includes("/permissions")) return t("permissionChanged");
    if (event.includes("/roles")) return t("roleChanged");
    if (event.includes("/users")) return t("userChanged");
    if (event.includes("/clients")) return t("clientChanged");
    if (event.includes("/policy")) return t("policyChanged");
    return t("activity");
}
