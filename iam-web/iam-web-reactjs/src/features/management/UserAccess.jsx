import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { ResourceState } from "@/components/workspace/ResourceState";

export function UserAccess({ id }) {
    const { t } = useWorkspace();
    const r = useResource(`/users/${id}/authorities`, true);
    return (
        <ResourceState resource={r}>
            {(a) => (
                <div className="two-columns grid grid-cols-[1.4fr_1fr] items-start gap-5 max-[700px]:grid-cols-1 [.dashboard-root_&]:grid-cols-[minmax(0,.9fr)_minmax(0,1.2fr)] [.dashboard-root_&]:items-stretch [.dashboard-root_&]:max-md:grid-cols-1 workspace-section mt-6">
                    <section
                        aria-label={t("roles")}
                        className="card block min-w-0 rounded-2xl border border-border bg-card p-[clamp(1.25rem,4vw,2rem)] shadow-none [.dashboard-root_&]:rounded-[10px] [.dashboard-root_&]:p-[1.4rem]"
                    >
                        <h3>{t("roles")}</h3>
                        {!a.roles.length && <p>{t("empty")}</p>}
                        {a.roles.map((role) => (
                            <p key={role.uuid}>
                                {role.name} <code>{role.uuid}</code>
                            </p>
                        ))}
                    </section>
                    <section
                        aria-label={t("permissions")}
                        className="card block min-w-0 rounded-2xl border border-border bg-card p-[clamp(1.25rem,4vw,2rem)] shadow-none [.dashboard-root_&]:rounded-[10px] [.dashboard-root_&]:p-[1.4rem]"
                    >
                        <h3>{t("permissions")}</h3>
                        {a.directPermissions.map((p) => (
                            <p key={p.name}>
                                {p.name} · {p.effect}
                            </p>
                        ))}
                        <p>{a.effectivePermissions.join(", ") || "—"}</p>
                    </section>
                </div>
            )}
        </ResourceState>
    );
}
