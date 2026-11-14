import { z } from "zod";
import {
    requiredText,
    optionalText,
    email,
    password,
    username,
    registrationUsername,
    httpUrl,
    integer,
    uuid,
    passwordChange,
} from "./common.js";
export { passwordChange };
export const reasonSchema = z.object({ reason: requiredText(500) });
export const reviewSchema = z.object({ reviewReason: requiredText(500) });
const activityDate = optionalText(10).refine(
    (value) =>
        !value ||
        (/^\d{4}-\d{2}-\d{2}$/.test(value) &&
            !Number.isNaN(Date.parse(value)) &&
            new Date(value).toISOString().slice(0, 10) === value),
    "validation.value",
);
export const activityFilterSchema = z
    .object({
        actor: optionalText(36).refine(
            (value) => !value || z.guid().safeParse(value).success,
            "validation.value",
        ),
        target: optionalText(200),
        from: activityDate,
        to: activityDate,
        suspicious: z.boolean().optional(),
    })
    .refine((value) => !value.from || !value.to || value.from <= value.to, {
        path: ["to"],
        message: "validation.value",
    });
export const policySchema = z.object({
    sessionMinutes: integer(5, 1440),
    accessMinutes: integer(1, 60),
    refreshMinutes: integer(5, 43200),
    passwordMinimum: integer(8, 64),
    requireNumber: z.boolean().optional(),
    requireUppercase: z.boolean().optional(),
    requireConsent: z.boolean().optional(),
});
export function recordSchema(kind, fresh) {
    if (kind === "users")
        return z.object({
            username: fresh ? registrationUsername : username,
            email,
            givenName: requiredText(100),
            familyName: requiredText(100),
            ...(fresh
                ? { password }
                : {
                      phoneNumber: optionalText().refine(
                          (value) =>
                              !value || /^\+[1-9][0-9]{1,14}$/.test(value),
                          "validation.value",
                      ),
                      pictureUrl: httpUrl,
                  }),
        });
    if (kind === "roles")
        return z.object({
            name: requiredText(100),
            description: optionalText(),
        });
    if (kind === "permissions")
        return z.object({
            resource: requiredText(100),
            action: requiredText(100),
            description: optionalText(),
        });
    return z
        .object({
            clientId: requiredText(100),
            clientName: requiredText(200),
            clientSecret: optionalText(72).refine(
                (value) =>
                    !value ||
                    (value.length >= 24 && /^[\x20-\x7e]+$/.test(value)),
                "validation.value",
            ),
            confidential: z.boolean().optional(),
            grantTypes: requiredText(),
            redirectUris: optionalText(),
            scopes: requiredText(),
            accessMinutes: integer(1, 60),
            refreshMinutes: integer(5, 43200),
        })
        .refine(
            (value) =>
                !fresh || !value.confidential || Boolean(value.clientSecret),
            { message: "validation.required", path: ["clientSecret"] },
        );
}
export function assignmentSchema(kind, target) {
    return z.object({
        [kind === "roles" ? "roleId" : "permissionId"]: uuid,
        [`${target}-${kind}-reason`]: requiredText(500),
        effect: z
            .enum(["GRANT", "DENY"], { error: "validation.value" })
            .optional(),
    });
}
