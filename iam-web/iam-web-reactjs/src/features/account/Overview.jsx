import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { ResourceState } from "@/components/workspace/ResourceState";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import {
    ShieldCheck,
    KeyRound,
    AppWindow,
    UsersRound,
    Activity as ActivityIcon,
} from "lucide-react";
import { ProfileAvatar } from "@/components/ProfileAvatar";
import { AppLink } from "@/components/AppLink";
import { Badge } from "@/components/ui/badge";
import { activityLabel } from "@/lib/activity-label";
import { DeactivationRequests } from "@/features/management/DeactivationRequests";

export function Overview() {
    const { t, data, url, can, formatDate, language } = useWorkspace();
    const resource = useResource("/overview");
    const activities = useResource(
        `/activity?size=5&audit=${Boolean(can("audit:read"))}`,
    );
    return (
        <>
            <ResourceState resource={resource}>
                {(stats) => (
                    <div className="workspace-stats grid grid-cols-[repeat(auto-fit,minmax(150px,1fr))] gap-4 max-md:grid-cols-2 max-md:gap-3">
                        {Object.entries(stats).map(([key, value]) => (
                            <Card
                                className="card dashboard-stat flex flex-col gap-[.6rem] rounded-[10px] px-0 py-[1.1rem] shadow-[0_1px_2px_rgb(15_23_42/.025)] [&_[data-slot=card-header]]:flex [&_[data-slot=card-header]]:flex-row [&_[data-slot=card-header]]:items-center [&_[data-slot=card-header]]:justify-between [&_[data-slot=card-header]]:px-[1.1rem] [&_[data-slot=card-title]]:text-[.74rem] [&_[data-slot=card-title]]:font-medium [&_[data-slot=card-title]]:text-slate-600 [&_[data-slot=card-content]]:flex [&_[data-slot=card-content]]:items-baseline [&_[data-slot=card-content]]:gap-[.6rem] [&_[data-slot=card-content]]:px-[1.1rem] [&_strong]:text-[1.9rem] [&_strong]:leading-[1.3] [&_strong]:font-[650] [&_strong]:tracking-[-.04em] [&_strong]:tabular-nums [&_[data-slot=card-content]>span]:text-[.65rem] [&_[data-slot=card-content]>span]:text-slate-500"
                                key={key}
                            >
                                <CardHeader>
                                    <CardTitle>{t(key)}</CardTitle>
                                    <span
                                        className="dashboard-stat-icon grid size-[29px] place-items-center rounded-[7px] bg-accent text-primary [&_svg]:size-[15px]"
                                        aria-hidden="true"
                                    >
                                        {key === "roles" ? (
                                            <ShieldCheck />
                                        ) : key === "permissions" ? (
                                            <KeyRound />
                                        ) : key === "clients" ? (
                                            <AppWindow />
                                        ) : (
                                            <UsersRound />
                                        )}
                                    </span>
                                </CardHeader>
                                <CardContent>
                                    <strong>
                                        {new Intl.NumberFormat(
                                            language === "km"
                                                ? "km-KH-u-nu-khmr"
                                                : "en-GB",
                                        ).format(value)}
                                    </strong>
                                    <span>{t("total")}</span>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                )}
            </ResourceState>
            <div className="two-columns grid grid-cols-[1.4fr_1fr] items-start gap-5 max-[700px]:grid-cols-1 [.dashboard-root_&]:grid-cols-[minmax(0,.9fr)_minmax(0,1.2fr)] [.dashboard-root_&]:items-stretch [.dashboard-root_&]:max-md:grid-cols-1 workspace-section mt-6">
                <section className="card block min-w-0 rounded-2xl border border-border bg-card p-[clamp(1.25rem,4vw,2rem)] shadow-none [.dashboard-root_&]:rounded-[10px] [.dashboard-root_&]:p-[1.4rem]">
                    <div className="mb-4 flex items-center gap-3">
                        <ProfileAvatar
                            src={data.claims?.pictureUrl}
                            name={[
                                data.claims?.givenName,
                                data.claims?.familyName,
                            ]
                                .filter(Boolean)
                                .join(" ")}
                            className="size-12 text-base"
                        />
                        <h2>
                            {data.claims?.givenName} {data.claims?.familyName}
                        </h2>
                    </div>
                    <p>{data.claims?.email}</p>
                    <p>{data.claims?.username}</p>
                    <AppLink
                        className="button inline-flex items-center justify-center gap-2 rounded-lg border border-primary bg-card px-[1.125rem] py-3 text-center font-semibold text-brand-800 no-underline hover:bg-accent active:bg-brand-100 [.dashboard-root_&]:rounded-md [.dashboard-root_&]:border-border [.dashboard-root_&]:px-[.8rem] [.dashboard-root_&]:py-[.45rem] [.dashboard-root_&]:text-xs secondary border-primary bg-card text-brand-800 hover:bg-accent active:bg-brand-100"
                        href={url("/profile/edit")}
                    >
                        {t("profile")}
                    </AppLink>
                    <h3>{t("access")}</h3>
                    <div className="dashboard-role-badges flex flex-wrap gap-[.4rem]">
                        {data.roles?.map((role) => (
                            <Badge variant="secondary" key={role}>
                                {role}
                            </Badge>
                        ))}
                    </div>
                </section>
                <section className="card block min-w-0 rounded-2xl border border-border bg-card p-[clamp(1.25rem,4vw,2rem)] shadow-none [.dashboard-root_&]:rounded-[10px] [.dashboard-root_&]:p-[1.4rem]">
                    <h2>
                        {t(can("audit:read") ? "recentActivities" : "activity")}
                    </h2>
                    <ResourceState resource={activities}>
                        {(result) =>
                            result.content.length ? (
                                <ul className="workspace-timeline m-0 list-none p-0 [&_li]:flex [&_li]:items-start [&_li]:justify-between [&_li]:gap-4 [&_li]:border-b [&_li]:border-border [&_li]:py-[.85rem] [&_li]:text-[.76rem] [&_li]:wrap-anywhere [&_li:last-child]:border-0 max-md:[&_li]:flex-col max-md:[&_li]:gap-[.35rem] [&_time]:text-[.67rem] [&_time]:whitespace-nowrap [&_time]:text-slate-500">
                                    {result.content.map((row) => (
                                        <li key={row.id}>
                                            <span className="dashboard-event flex items-center gap-[.6rem] [&_svg]:shrink-0 [&_svg]:text-slate-400">
                                                <ActivityIcon
                                                    aria-hidden="true"
                                                    size={15}
                                                />
                                                {activityLabel(row.event, t)}
                                                {can("audit:read") &&
                                                    row.actor_username && (
                                                        <span className="text-muted-foreground">
                                                            ·{" "}
                                                            {row.actor_username}
                                                        </span>
                                                    )}
                                            </span>
                                            <time>
                                                {formatDate(row.occurred_at)}
                                            </time>
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <p>{t("empty")}</p>
                            )
                        }
                    </ResourceState>
                </section>
            </div>
            {can("user:read") && <DeactivationRequests />}
        </>
    );
}
