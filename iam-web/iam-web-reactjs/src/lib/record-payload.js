import { split } from "@/lib/workspace-utils";
export function buildRecordPayload({ kind, fresh, form, record }) {
    const fields = Object.fromEntries(new FormData(form));
    let body;
    if (kind === "users")
        body = fresh
            ? {
                  username: fields.username,
                  email: fields.email,
                  givenName: fields.givenName,
                  familyName: fields.familyName,
                  password: fields.password,
              }
            : {
                  username: fields.username,
                  email: fields.email,
                  givenName: fields.givenName,
                  familyName: fields.familyName,
                  phoneNumber: fields.phoneNumber || null,
                  pictureUrl: fields.pictureUrl || null,
                  version: record.version,
              };
    else if (kind === "roles")
        body = {
            name: fields.name,
            description: fields.description,
        };
    else if (kind === "permissions")
        body = {
            resource: fields.resource,
            action: fields.action,
            description: fields.description,
        };
    else
        body = {
            clientId: fields.clientId,
            clientName: fields.clientName,
            clientSecret: fields.clientSecret,
            confidential: fields.confidential === "on",
            grantTypes: split(fields.grantTypes),
            redirectUris: split(fields.redirectUris),
            scopes: split(fields.scopes),
            accessMinutes: Number(fields.accessMinutes),
            refreshMinutes: Number(fields.refreshMinutes),
        };

    return body;
}
