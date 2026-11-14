import { useWorkspace } from "@/context/workspace-context";
import { Badge } from "@/components/ui/badge";

export function UserAccountStatus({ user }) {
    const { t } = useWorkspace();
    return (
        <section className="my-4" aria-label={t("accountStatus")}>
            <h3>{t("accountStatus")}</h3>
            <div className="mt-2 flex flex-wrap gap-2">
                <Badge variant={user.enabled ? "secondary" : "destructive"}>
                    {t(user.enabled ? "enabled" : "disabled")}
                </Badge>
                <Badge
                    variant={
                        user.accountNonLocked ? "secondary" : "destructive"
                    }
                >
                    {t(user.accountNonLocked ? "unlocked" : "locked")}
                </Badge>
            </div>
        </section>
    );
}
