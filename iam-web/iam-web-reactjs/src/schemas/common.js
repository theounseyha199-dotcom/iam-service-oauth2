import { z } from "zod";
const text = (schema) => z.preprocess((value) => value ?? "", schema);
export const requiredText = (max = 500) =>
    text(
        z
            .string()
            .max(max, "validation.value")
            .refine((value) => value.trim().length > 0, "validation.required"),
    );
export const optionalText = (max = 500) =>
    text(z.string().max(max, "validation.value"));
export const email = requiredText(255).refine(
    (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim()),
    "validation.email",
);
export const password = requiredText(1000).refine(
    (value) =>
        value.length >= 8 && new TextEncoder().encode(value).length <= 72,
    "validation.password",
);
export const username = requiredText(50).refine(
    (value) => value === value.trim(),
    "validation.username",
);
export const registrationUsername = username.refine(
    (value) => /^[A-Za-z0-9._-]{3,50}$/.test(value),
    "validation.username",
);
export const httpUrl = optionalText().refine((value) => {
    if (!value.trim()) return true;
    try {
        const url = new URL(value.trim());
        return (
            ["http:", "https:"].includes(url.protocol) && Boolean(url.hostname)
        );
    } catch {
        return false;
    }
}, "validation.value");
export const integer = (min, max) =>
    requiredText().refine(
        (value) =>
            Number.isInteger(Number(value)) &&
            Number(value) >= min &&
            Number(value) <= max,
        "validation.value",
    );
export const confirmPassword = (schema, field = "newPassword") =>
    schema.refine((values) => values[field] === values.confirmPassword, {
        message: "validation.confirm",
        path: ["confirmPassword"],
    });
export const passwordChange = confirmPassword(
    z.object({ newPassword: password, confirmPassword: requiredText(1000) }),
);
export const uuid = requiredText().refine(
    // Seeded role/permission IDs are canonical GUIDs without RFC version/variant bits.
    (value) => z.guid().safeParse(value.trim()).success,
    "validation.value",
);
