import { createInstance } from "i18next";
import { initReactI18next } from "react-i18next";
import en from "./resources/en/workspace.json" with { type: "json" };
import km from "./resources/km/workspace.json" with { type: "json" };

export const i18n = createInstance();
void i18n.use(initReactI18next).init({
    lng: "en",
    fallbackLng: "en",
    supportedLngs: ["en", "km"],
    ns: ["account", "workspace"],
    defaultNS: "account",
    keySeparator: false,
    resources: { en: { workspace: en }, km: { workspace: km } },
    interpolation: { escapeValue: false },
    returnNull: false,
    initAsync: false,
    react: { useSuspense: false },
});
