import { useCallback } from "react";
import { useTranslation } from "react-i18next";

export function useAccountTranslation(language) {
    const { t, i18n } = useTranslation("account", { lng: language });
    return useCallback(
        (key, ...args) => {
            if (!i18n.exists(key, { lng: language, ns: "account" })) {
                if (key.startsWith("scope.")) return key.slice(6);
                key = "error.generic";
            }
            return t(
                key,
                Object.fromEntries(
                    args.map((value, index) => [`arg${index}`, value]),
                ),
            );
        },
        [t, i18n, language],
    );
}
