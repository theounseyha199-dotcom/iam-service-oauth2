import { recordSchema } from "@/schemas/workspace";
import { useWorkspace } from "@/context/workspace-context";
import { useState, useEffect } from "react";
import { useResource } from "@/hooks/use-resource";
import { managementResources as resources } from "@/lib/dashboard-navigation";
import { buildRecordPayload } from "@/lib/record-payload";
import { flushSync } from "react-dom";
import { Button } from "@/components/ui/button";
import { ResourceState } from "@/components/workspace/ResourceState";
import { ActionForm } from "@/components/workspace/ActionForm";
import { RecordFields } from "@/features/management/fields/RecordFields";

import { UserTools } from "@/features/management/UserTools";
import { RoleAccess } from "@/features/management/RoleAccess";
import { Assignment } from "@/features/management/Assignment";

export function RecordEditor({ kind, id, done }) {
    const { t, api, can, confirm, setAppDirty } = useWorkspace();
    const [dirty, setDirty] = useState(false);
    const fresh = id === "new";
    const path = kind === "users" ? `/users/${id}/profile` : `/${kind}/${id}`;
    const resource = useResource(
        fresh ? (kind === "clients" ? "/client-defaults" : "/overview") : path,
    );
    const editable = can(resources[kind] + (fresh ? ":create" : ":update"));
    useEffect(() => {
        setAppDirty(dirty);
        return () => setAppDirty(false);
    }, [dirty, setAppDirty]);
    function back() {
        if (dirty)
            confirm({
                label: "discard",
                object: t("unsaved"),
                run: () => {
                    flushSync(() => {
                        setDirty(false);
                        setAppDirty(false);
                    });
                    done();
                },
            });
        else done();
    }
    async function save(form) {
        const body = buildRecordPayload({
            kind,
            fresh,
            form,
            record: resource.data,
        });
        await api(`/${kind}${fresh ? "" : "/" + id}`, {
            method: fresh ? "POST" : kind === "clients" ? "PUT" : "PATCH",
            body,
        });
        flushSync(() => {
            setDirty(false);
            setAppDirty(false);
        });
        done();
    }
    return (
        <>
            <Button variant="outline" onClick={back}>
                {t("back")}
            </Button>
            <ResourceState resource={resource}>
                {(record) => {
                    const row = fresh && kind !== "clients" ? {} : record;
                    return (
                        <>
                            <h2 className="workspace-section mt-6">
                                {fresh
                                    ? t("create")
                                    : (row.username ??
                                      row.name ??
                                      row.clientName)}
                            </h2>
                            <ActionForm
                                schema={recordSchema(kind, fresh)}
                                disabled={!editable}
                                onSave={save}
                                onDirty={() => setDirty(true)}
                            >
                                <fieldset disabled={!editable}>
                                    <div className="form-grid grid grid-cols-2 gap-x-5 gap-y-3 max-[700px]:grid-cols-1 [.dashboard-root_&]:max-md:grid-cols-1">
                                        {
                                            <RecordFields
                                                kind={kind}
                                                row={row}
                                                fresh={fresh}
                                            />
                                        }
                                    </div>
                                </fieldset>
                            </ActionForm>
                            {!fresh && kind === "users" && (
                                <UserTools
                                    id={id}
                                    row={row}
                                    reload={resource.reload}
                                />
                            )}{" "}
                            {!fresh && kind === "roles" && (
                                <>
                                    <RoleAccess id={id} />
                                    {can("role:assign") && (
                                        <Assignment
                                            id={id}
                                            target="role"
                                            kind="permissions"
                                        />
                                    )}
                                </>
                            )}
                        </>
                    );
                }}
            </ResourceState>
        </>
    );
}
