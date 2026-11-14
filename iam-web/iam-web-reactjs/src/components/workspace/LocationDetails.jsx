import { useWorkspace } from "@/context/workspace-context";

export function LocationDetails({ location }) {
    const { t, language } = useWorkspace();
    if (location?.status === "local") return <span>{t("localNetwork")}</span>;
    if (location?.status !== "approximate") return <span>{t("unknown")}</span>;
    const country = location.country
        ? new Intl.DisplayNames([language === "km" ? "km" : "en"], {
              type: "region",
          }).of(location.country)
        : "";
    const name = [location.city, country].filter(Boolean).join(", ");
    return name ? (
        <span className="grid gap-1">
            <span>{name}</span>
            <span className="text-xs text-muted-foreground">
                {t("approximateLocation")}
            </span>
        </span>
    ) : (
        <span>{t("unknown")}</span>
    );
}
