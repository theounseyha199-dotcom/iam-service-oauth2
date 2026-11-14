import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { Button } from "@/components/ui/button";
import { ResourceState } from "@/components/workspace/ResourceState";
import { Table } from "@/components/workspace/DataTable";
import { DeviceDetails } from "@/components/workspace/DeviceDetails";
import { LocationDetails } from "@/components/workspace/LocationDetails";

export function Sessions({ user }) {
    const { t, api, confirm, notify, url, formatDate, can } = useWorkspace();
    const query = user ? `?user=${encodeURIComponent(user)}` : "";
    const resource = useResource("/sessions" + query);
    function end(key, current = false) {
        confirm({
            label: key === "all" ? "terminateAll" : "terminate",
            object: key === "all" ? t("terminateAll") : t("terminate"),
            description: t(
                key === "all"
                    ? "terminateAllWarning"
                    : current
                      ? "terminateCurrentWarning"
                      : "terminateWarning",
            ),
            run: async () => {
                await api("/sessions/" + key + query, {
                    method: "DELETE",
                });
                if (current || (key === "all" && !user))
                    window.location.assign(url("/login"));
                else {
                    resource.reload();
                    notify(t("saved"));
                }
            },
        });
    }
    const editable = !user || can("user:update");
    return (
        <>
            <p className="hint text-sm text-muted-foreground [.page_&]:m-0! [.page_&]:rounded [.page_&]:border-s-2 [.page_&]:border-brand-200 [.page_&]:bg-slate-50 [.page_&]:px-4 [.page_&]:py-3 [.dashboard-root_&]:text-xs">
                {t("sessionNote")}
            </p>
            {editable && (
                <Button variant="outline" onClick={() => end("all")}>
                    {t("terminateAll")}
                </Button>
            )}
            <ResourceState resource={resource}>
                {(rows) => (
                    <Table
                        rows={rows}
                        rowKey="id"
                        columns={[
                            {
                                key: "device",
                                render: (row) => (
                                    <>
                                        <span>
                                            <DeviceDetails value={row.device} />
                                        </span>
                                        {row.current && (
                                            <span className="workspace-badge m-[.15rem] inline-block rounded-[5px] border border-border bg-accent px-2 py-[.15rem] text-[.68rem] text-primary">
                                                {t("current")}
                                            </span>
                                        )}
                                    </>
                                ),
                            },
                            {
                                key: "ip",
                            },
                            {
                                key: "location",
                                render: (row) => (
                                    <LocationDetails location={row.location} />
                                ),
                            },
                            {
                                key: "lastActivity",
                                render: (r) => formatDate(r.lastActivity),
                            },
                            ...(editable
                                ? [
                                      {
                                          key: "actions",
                                          render: (row) => (
                                              <Button
                                                  variant="outline"
                                                  onClick={() =>
                                                      end(row.id, row.current)
                                                  }
                                              >
                                                  {t("terminate")}
                                              </Button>
                                          ),
                                      },
                                  ]
                                : []),
                        ]}
                    />
                )}
            </ResourceState>
        </>
    );
}
