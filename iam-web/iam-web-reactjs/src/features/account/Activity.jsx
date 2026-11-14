import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { Search } from "@/components/workspace/Search";
import { ResourceState } from "@/components/workspace/ResourceState";
import { Table } from "@/components/workspace/DataTable";
import { activityLabel } from "@/lib/activity-label";
import { DeviceDetails } from "@/components/workspace/DeviceDetails";
import { Pager } from "@/components/workspace/Pager";
import { ActivityFilters } from "@/features/account/ActivityFilters";

export function Activity({ history = false, audit = false, user }) {
    const { t, state, setQuery, formatDate } = useWorkspace();
    const event = history ? "login" : (state.search ?? "");
    const status = state.status ?? "";
    const page = Number(state.page) || 0;
    const params = new URLSearchParams({
        page: String(page),
        size: "20",
        event,
        audit: String(audit),
    });
    if (user) params.set("user", user);
    if (status) params.set("success", status);
    for (const key of ["actor", "target", "from", "to", "suspicious"]) {
        if (state[key] && (audit || !["actor", "target"].includes(key)))
            params.set(key, state[key]);
    }
    const resource = useResource("/activity?" + params);
    return (
        <>
            <div className="workspace-toolbar my-4 flex flex-wrap items-end justify-between gap-4 [&_form]:m-0 [&_form]:max-w-[380px] [&_form]:flex-1 [&_select]:mt-[.4rem]">
                {!history && (
                    <Search
                        value={event}
                        onChange={(search) =>
                            setQuery({
                                search,
                                page: 0,
                            })
                        }
                    />
                )}
                <label>
                    {t("status")}{" "}
                    <select
                        value={status}
                        onChange={(e) =>
                            setQuery({
                                status: e.target.value,
                                page: 0,
                            })
                        }
                    >
                        <option value="">{t("all")}</option>
                        <option value="true">{t("success")}</option>
                        <option value="false">{t("failed")}</option>
                    </select>
                </label>
            </div>
            <ActivityFilters audit={audit} />
            <ResourceState resource={resource}>
                {(result) => (
                    <>
                        <Table
                            rows={result.content}
                            rowKey="id"
                            columns={[
                                {
                                    key: "occurred_at",
                                    label: "timestamp",
                                    render: (r) => formatDate(r.occurred_at),
                                },
                                ...(audit
                                    ? [
                                          {
                                              key: "subject",
                                              label: "actor",
                                              render: (row) =>
                                                  row.actor_username ||
                                                  row.subject,
                                          },
                                          { key: "target" },
                                      ]
                                    : []),
                                {
                                    key: "event",
                                    render: (r) => activityLabel(r.event, t),
                                },
                                {
                                    key: "status",
                                    render: (r) => (
                                        <span
                                            className={`workspace-badge m-[.15rem] inline-block rounded-[5px] border border-border bg-accent px-2 py-[.15rem] text-[.68rem] text-primary ${r.success ? "" : "failed bg-card! text-destructive!"}`}
                                        >
                                            {t(
                                                r.success
                                                    ? "success"
                                                    : "failed",
                                            )}
                                        </span>
                                    ),
                                },
                                {
                                    key: "ip",
                                },
                                {
                                    key: "suspicious",
                                    label: "securityIndicator",
                                    render: (row) =>
                                        row.suspicious ? (
                                            <span className="font-medium text-destructive">
                                                {t("repeatedFailures")}
                                            </span>
                                        ) : (
                                            "—"
                                        ),
                                },
                                {
                                    key: "device",
                                    render: (row) => (
                                        <DeviceDetails value={row.device} />
                                    ),
                                },
                            ]}
                        />
                        <Pager
                            metadata={result.page}
                            onPage={(page) =>
                                setQuery({
                                    page,
                                })
                            }
                        />
                    </>
                )}
            </ResourceState>
        </>
    );
}
