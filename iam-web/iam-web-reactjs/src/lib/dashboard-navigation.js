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
export function dashboardNavigation(permissions = [], roles = []) {
    const userOnly = roles.length > 0 && roles.every((role) => role === "USER");
    const managementPermissions = userOnly ? [] : permissions;
    return {
        nav: accountNavigation,
        admin: [
            ...(managementPermissions.includes("user:read")
                ? [["reviews"]]
                : []),
            ...Object.entries(managementResources)
                .filter(([, resource]) =>
                    managementPermissions.includes(resource + ":read"),
                )
                .map(([key]) => [key]),
            ...(managementPermissions.includes("audit:read")
                ? [["audit"]]
                : []),
            ...(managementPermissions.includes("policy:read")
                ? [["policy"]]
                : []),
        ],
    };
}
