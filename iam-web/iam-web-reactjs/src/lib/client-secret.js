export function generateClientSecret() {
    if (!globalThis.crypto?.getRandomValues) {
        throw new Error("Secure random generation is unavailable");
    }
    const bytes = crypto.getRandomValues(new Uint8Array(32));
    return btoa(String.fromCharCode(...bytes))
        .replaceAll("+", "-")
        .replaceAll("/", "_")
        .replace(/=+$/, "");
}
