export const split = (value) =>
    value
        .split(",")
        .map((v) => v.trim())
        .filter(Boolean);

export const valueOf = (form, name) =>
    new FormData(form).get(name)?.toString() ?? "";
