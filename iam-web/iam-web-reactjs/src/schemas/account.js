import { z } from "zod";
import {
    requiredText,
    optionalText,
    email,
    password,
    username,
    registrationUsername,
    httpUrl,
    passwordChange,
    confirmPassword,
} from "./common.js";

const registration = confirmPassword(
    z.object({
        username: registrationUsername,
        email,
        givenName: requiredText(100),
        familyName: requiredText(100),
        password,
        confirmPassword: requiredText(1000),
    }),
    "password",
);
const profile = z.object({
    username,
    email,
    givenName: requiredText(100),
    familyName: requiredText(100),
    middleName: optionalText(100),
    nickname: optionalText(100),
    gender: z.enum(["", "female", "male", "other"], {
        error: "validation.value",
    }),
    locale: z.enum(["en", "km"], { error: "validation.value" }),
    birthdate: optionalText().refine(
        (value) =>
            !value ||
            (/^\d{4}-\d{2}-\d{2}$/.test(value) &&
                !Number.isNaN(Date.parse(value)) &&
                new Date(value).toISOString().slice(0, 10) === value),
        "validation.value",
    ),
    profileUrl: httpUrl,
    pictureUrl: httpUrl,
    websiteUrl: httpUrl,
    zoneinfo: optionalText(100),
    phoneNumber: optionalText().refine(
        (value) => !value.trim() || /^\+[1-9][0-9]{1,14}$/.test(value.trim()),
        "validation.value",
    ),
    streetAddress: optionalText(255),
    locality: optionalText(100),
    region: optionalText(100),
    postalCode: optionalText(20),
    country: optionalText(2).refine(
        (value) => !value || /^[A-Za-z]{2}$/.test(value),
        "validation.value",
    ),
});
export function accountSchema(action, data = {}) {
    switch (action) {
        case "/login":
            return z.object({
                username: requiredText(255),
                password: requiredText(1000),
            });
        case "/register":
            return registration;
        case "/profile/edit":
            return profile;
        case "/profile/password":
            return passwordChange.safeExtend({
                currentPassword: requiredText(1000),
            });
        case "/reset-password":
            return passwordChange;
        case "/forgot-password":
        case "/resend-verification":
            return z.object({ email });
        case "/verify-email":
            return z.object({
                email,
                code: requiredText(6).refine(
                    (value) => /^\d{6}$/.test(value),
                    "verification.codeError",
                ),
            });
        case "/federation/complete":
            return z.object({
                ...(data.needEmail ? { email } : {}),
                ...(data.needGivenName ? { givenName: requiredText(100) } : {}),
                ...(data.needFamilyName
                    ? { familyName: requiredText(100) }
                    : {}),
            });
        default:
            return z.object({}); // CSRF/protocol-only forms retain their native hidden fields.
    }
}
