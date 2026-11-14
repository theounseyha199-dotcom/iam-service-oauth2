import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { ResourceState } from "@/components/workspace/ResourceState";
import { Table } from "@/components/workspace/DataTable";

export function RoleAccess({ id }) {
    const { t } = useWorkspace();
    const r = useResource(`/roles/${id}/permissions`, true);
    return (
        <section className="workspace-section mt-6">
            <h3>{t("rolePermissions")}</h3>
            <ResourceState resource={r}>
                {(rows) => (
                    <Table
                        rows={rows}
                        columns={[
                            {
                                key: "name",
                            },
                            {
                                key: "uuid",
                            },
                        ]}
                    />
                )}
            </ResourceState>
        </section>
    );
}
