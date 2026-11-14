import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { accountSchema } from "../iam-web/iam-web-reactjs/src/schemas/account.js";
import {
    recordSchema,
    policySchema,
    assignmentSchema,
    reviewSchema,
    activityFilterSchema,
} from "../iam-web/iam-web-reactjs/src/schemas/workspace.js";
import { prepareBootstrap } from "../iam-web/iam-web-reactjs/src/i18n/prepare-bootstrap.js";
import { i18n } from "../iam-web/iam-web-reactjs/src/i18n/instance.js";

const invalid = (schema, values, field) => {
    const result = schema.safeParse(values);
    assert.equal(result.success, false);
    assert.ok(
        result.error.issues.some((issue) => issue.path[0] === field),
        JSON.stringify(result.error.issues),
    );
};
assert.equal(
    assignmentSchema("roles", "user").safeParse({
        roleId: "00000000-0000-0000-0000-000000000002",
        "user-roles-reason": "Assign the seeded USER role",
    }).success,
    true,
);
invalid(reviewSchema, { reviewReason: "   " }, "reviewReason");
invalid(reviewSchema, { reviewReason: "x".repeat(501) }, "reviewReason");
invalid(activityFilterSchema, { actor: "invalid" }, "actor");
invalid(activityFilterSchema, { from: "2026-02-30" }, "from");
invalid(activityFilterSchema, { from: "2026-11-15", to: "2026-11-01" }, "to");
assert.equal(
    activityFilterSchema.safeParse({ from: "2026-11-01", to: "2026-11-15" })
        .success,
    true,
);
assert.equal(
    accountSchema("/login").safeParse({ username: "legacy", password: "short" })
        .success,
    true,
);
invalid(accountSchema("/login"), { username: "  ", password: "" }, "username");
const registration = {
    username: "new.user",
    email: "new@example.com",
    givenName: "Dara",
    familyName: "Sok",
    password: "Str0ng!Pass2026",
    confirmPassword: "Str0ng!Pass2026",
};
assert.equal(accountSchema("/register").safeParse(registration).success, true);
invalid(
    accountSchema("/register"),
    { ...registration, username: "a" },
    "username",
);
invalid(
    accountSchema("/register"),
    { ...registration, confirmPassword: "different" },
    "confirmPassword",
);
invalid(
    accountSchema("/register"),
    {
        ...registration,
        password: "ក".repeat(30),
        confirmPassword: "ក".repeat(30),
    },
    "password",
);
invalid(
    accountSchema("/verify-email"),
    { email: "new@example.com", code: "abcdef" },
    "code",
);
assert.equal(
    accountSchema("/verify-email").safeParse({
        email: "new@example.com",
        code: "012345",
    }).success,
    true,
);
assert.equal(
    accountSchema("/federation/complete", { needGivenName: true }).safeParse({
        givenName: "Dara",
    }).success,
    true,
);
invalid(accountSchema("/forgot-password"), { email: "invalid" }, "email");
invalid(
    recordSchema("users", true),
    { ...registration, email: "invalid" },
    "email",
);
const client = {
    clientId: "test",
    clientName: "Test",
    clientSecret: "",
    confidential: true,
    grantTypes: "authorization_code",
    redirectUris: "https://example.com",
    scopes: "openid",
    accessMinutes: "5",
    refreshMinutes: "1440",
};
invalid(recordSchema("clients", true), client, "clientSecret");
assert.equal(recordSchema("clients", false).safeParse(client).success, true);
assert.equal(
    recordSchema("clients", true).safeParse({ ...client, confidential: false })
        .success,
    true,
);
invalid(
    policySchema,
    {
        sessionMinutes: "0",
        accessMinutes: "5",
        refreshMinutes: "1440",
        passwordMinimum: "8",
    },
    "sessionMinutes",
);
invalid(
    assignmentSchema("roles", "user"),
    { roleId: "wrong", "user-roles-reason": "Review" },
    "roleId",
);

const en = JSON.parse(
    await readFile(
        new URL(
            "../iam-web/iam-web-reactjs/src/i18n/resources/en/workspace.json",
            import.meta.url,
        ),
    ),
);
const km = JSON.parse(
    await readFile(
        new URL(
            "../iam-web/iam-web-reactjs/src/i18n/resources/km/workspace.json",
            import.meta.url,
        ),
    ),
);
assert.deepEqual(Object.keys(en).sort(), Object.keys(km).sort());
assert.ok(
    Object.values(km).every(
        (value) => typeof value === "string" && value.length > 0,
    ),
);
const english = {
    language: "en",
    messages: { example: "Hello {0}", "error.generic": "Try again" },
};
prepareBootstrap(english);
assert.equal(
    i18n.getFixedT("en", "account")("example", { arg0: "Dara" }),
    "Hello Dara",
);
assert.equal(english.messages.example, "Hello {0}");
prepareBootstrap({ language: "km", messages: { example: "សួស្តី {0}" } });
assert.equal(
    i18n.getFixedT("km", "account")("example", { arg0: "ដារ៉ា" }),
    "សួស្តី ដារ៉ា",
);
assert.equal(
    i18n.getFixedT("en", "account")("example", { arg0: "Dara" }),
    "Hello Dara",
);
assert.equal(i18n.getFixedT("km", "workspace")("overview"), km.overview);
console.log(
    "Zod schema and i18next checks passed: passwords, OTP, email, numeric bounds, UUIDs, client secrets, interpolation and English/Khmer resource parity.",
);
