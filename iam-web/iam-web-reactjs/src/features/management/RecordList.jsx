import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { managementResources as resources } from "@/lib/dashboard-navigation";
import { useState } from "react";
import { RecordEditor } from "@/features/management/RecordEditor";
import { Search } from "@/components/workspace/Search";
import { Button } from "@/components/ui/button";
import { ResourceState } from "@/components/workspace/ResourceState";
import { Table } from "@/components/workspace/DataTable";
import { Pager } from "@/components/workspace/Pager";

export function RecordList({ kind }) {
    const { t, api, can, confirm, notify, state, setQuery } = useWorkspace();
    const page = Number(state.page) || 0;
    const search = state.search ?? "";
    const resource = useResource(
        `/${kind}?page=${page}&size=20&search=${encodeURIComponent(search)}`,
    );
    const permission = resources[kind];
    const [editing, setEditing] = useState(null);
    if (editing)
        return (
            <RecordEditor
                key={editing}
                kind={kind}
                id={editing}
                done={() => {
                    setEditing(null);
                    resource.reload();
                }}
            />
        );
    const columns =
        kind === "users"
            ? [
                  {
                      key: "username",
                  },
                  {
                      key: "name",
                  },
                  {
                      key: "email",
                  },
                  {
                      key: "status",
                      render: (r) => (
                          <span className="workspace-badge m-[.15rem] inline-block rounded-[5px] border border-border bg-accent px-2 py-[.15rem] text-[.68rem] text-primary">
                              {t(r.enabled ? "enabled" : "disabled")}
                          </span>
                      ),
                  },
              ]
            : kind === "clients"
              ? [
                    {
                        key: "clientName",
                    },
                    {
                        key: "clientId",
                    },
                    {
                        key: "scopes",
                        render: (r) => r.scopes.join(", "),
                    },
                ]
              : kind === "permissions"
                ? [
                      {
                          key: "name",
                      },
                      {
                          key: "resource",
                      },
                      {
                          key: "action",
                      },
                      {
                          key: "description",
                      },
                  ]
                : [
                      {
                          key: "name",
                      },
                      {
                          key: "description",
                      },
                  ];
    return (
        <>
            <div className="workspace-toolbar my-4 flex flex-wrap items-end justify-between gap-4 [&_form]:m-0 [&_form]:max-w-[380px] [&_form]:flex-1 [&_select]:mt-[.4rem]">
                <Search
                    value={search}
                    onChange={(search) =>
                        setQuery({
                            search,
                            page: 0,
                        })
                    }
                />
                {can(permission + ":create") && (
                    <Button onClick={() => setEditing("new")}>
                        {t("create")} · {t(kind)}
                    </Button>
                )}
            </div>
            <ResourceState resource={resource}>
                {(result) => (
                    <>
                        <Table
                            rows={result.content}
                            columns={[
                                ...columns,
                                {
                                    key: "uuid",
                                    render: (r) => <code>{r.uuid}</code>,
                                },
                                {
                                    key: "actions",
                                    render: (row) => (
                                        <div className="workspace-row-actions flex flex-wrap gap-2 [&_button]:h-[30px] [&_button]:px-[.65rem] [&_button]:py-[.35rem] [&_button]:text-[.7rem]">
                                            <Button
                                                variant="outline"
                                                onClick={() =>
                                                    setEditing(row.uuid)
                                                }
                                            >
                                                {t(
                                                    can(permission + ":update")
                                                        ? "edit"
                                                        : "view",
                                                )}
                                            </Button>
                                            {can(permission + ":delete") && (
                                                <Button
                                                    variant="outline"
                                                    onClick={() =>
                                                        confirm({
                                                            label: "delete",
                                                            object:
                                                                row.username ??
                                                                row.name ??
                                                                row.clientName,
                                                            run: async () => {
                                                                await api(
                                                                    `/${kind}/${row.uuid}`,
                                                                    {
                                                                        method: "DELETE",
                                                                    },
                                                                );
                                                                if (
                                                                    result
                                                                        .content
                                                                        .length ===
                                                                        1 &&
                                                                    page > 0
                                                                )
                                                                    setQuery({
                                                                        page:
                                                                            page -
                                                                            1,
                                                                    });
                                                                else
                                                                    resource.reload();
                                                                notify(
                                                                    t("saved"),
                                                                );
                                                            },
                                                        })
                                                    }
                                                >
                                                    {t("delete")}
                                                </Button>
                                            )}
                                        </div>
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
