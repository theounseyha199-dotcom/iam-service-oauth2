import { i18n } from "./instance.js";

/** Keep the server message bundles authoritative for authentication/protocol pages. */
export function prepareBootstrap(bootstrap) {
    const language = bootstrap.language === "km" ? "km" : "en";
    const messages = Object.fromEntries(
        Object.entries(bootstrap.messages).map(([key, value]) => [
            key,
            value.replace(/\{(\d+)\}/g, "{{arg$1}}"),
        ]),
    );
    i18n.addResourceBundle(language, "account", messages, true, true);
    void i18n.changeLanguage(language);
    return bootstrap;
}
