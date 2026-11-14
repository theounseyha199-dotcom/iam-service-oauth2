export const managementResources = {
    users: "user",
    roles: "role",
    permissions: "permission",
    clients: "client",
};
const accountNavigation = [
    ["overview"],
    ["profile", "/profile/edit"],
    ["security", "/profile/password"],
    ["linked", "/profile/accounts"],
    ["apps", "/profile/apps"],
    ["sessions"],
    ["history"],
    ["activity"],
    ["deactivation"],
];
export const accountPanels = {
    "profile-edit": "profile",
    "change-password": "security",
    "linked-accounts": "linked",
    "unlink-account": "linked",
    "connected-apps": "apps",
    "revoke-app": "apps",
    consent: "apps",
};
export function dashboardNavigation(permissions = []) {
    return {
        nav: accountNavigation,
        admin: [
            ...(permissions.includes("user:read") ? [["reviews"]] : []),
            ...Object.entries(managementResources)
                .filter(([, resource]) =>
                    permissions.includes(resource + ":read"),
                )
                .map(([key]) => [key]),
            ...(permissions.includes("audit:read") ? [["audit"]] : []),
            ...(permissions.includes("policy:read") ? [["policy"]] : []),
        ],
    };
}
