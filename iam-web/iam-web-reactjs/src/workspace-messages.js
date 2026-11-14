import { i18n } from "@/i18n/instance";

/** Translation adapter for callbacks outside React components. */
export function workspaceText(language) {
    return i18n.getFixedT(language === "km" ? "km" : "en", "workspace");
}
