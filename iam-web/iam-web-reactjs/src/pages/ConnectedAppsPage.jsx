import { Notice } from "@/components/Notice";
import { ScopeList } from "@/components/account/ScopeList";
import { Link } from "@/components/account/AccountLink";
import { usePage } from "@/context/page-context";

export function ConnectedAppsPage() {
    const { data: d, t } = usePage();
    return (
        <>
            <p>{t("apps.intro")}</p>
            <Notice success message={d.revoked && t("apps.revoked")} />
            {d.apps?.length ? (
                <ul className="record-list list-none p-0 [&>li]:flex [&>li]:items-center [&>li]:justify-between [&>li]:gap-4 [&>li]:border-b [&>li]:border-border [&>li]:py-5 [&>li>div]:min-w-0 [&>li>div]:wrap-anywhere [&>li_a]:shrink-0 max-[700px]:[&>li]:flex-col max-[700px]:[&>li]:items-stretch">
                    {d.apps.map((app) => (
                        <li key={app.id}>
                            <div>
                                <h2>{app.name}</h2>
                                <ScopeList scopes={app.scopes} />
                            </div>
                            <Link
                                to={`/profile/apps/${encodeURIComponent(app.id)}/revoke`}
                                button
                                label="apps.revoke"
                            />
                        </li>
                    ))}
                </ul>
            ) : (
                <p className="empty">{t("apps.empty")}</p>
            )}
        </>
    );
}
