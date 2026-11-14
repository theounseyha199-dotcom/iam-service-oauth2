import { FormSelect } from "@/components/forms/FormSelect";
import { assignmentSchema } from "@/schemas/workspace";
import { useWorkspace } from "@/context/workspace-context";
import { useId, useState } from "react";
import { ActionForm } from "@/components/workspace/ActionForm";
import { valueOf } from "@/lib/workspace-utils";
import { Input } from "@/components/workspace/WorkspaceInput";
import { AssignmentPicker } from "@/features/management/AssignmentPicker";

export function Assignment({ id, kind, target = "user" }) {
    const { t, api, can, confirm, notify } = useWorkspace();
    const [mode, setMode] = useState("assign");
    const modeId = useId();
    const base = `/${target === "role" ? "roles" : "users"}/${id}/${kind}`;
    return (
        <section className="card block min-w-0 rounded-2xl border border-border bg-card p-[clamp(1.25rem,4vw,2rem)] shadow-none [.dashboard-root_&]:rounded-[10px] [.dashboard-root_&]:p-[1.4rem] workspace-section mt-6">
            <h3>
                {t(
                    target === "role"
                        ? "rolePermissions"
                        : kind === "roles"
                          ? "roleAssignment"
                          : "permissionAssignment",
                )}
            </h3>
            <p className="hint text-sm text-muted-foreground [.page_&]:m-0! [.page_&]:rounded [.page_&]:border-s-2 [.page_&]:border-brand-200 [.page_&]:bg-slate-50 [.page_&]:px-4 [.page_&]:py-3 [.dashboard-root_&]:text-xs">
                {t(
                    can(`${kind === "roles" ? "role" : "permission"}:read`)
                        ? "assignmentPickerHint"
                        : "assignmentHint",
                )}
            </p>
            <ActionForm
                schema={assignmentSchema(kind, target)}
                deferred
                label={mode}
                onSave={async (form) => {
                    const uuid = valueOf(
                        form,
                        kind === "roles" ? "roleId" : "permissionId",
                    ).trim();
                    if (
                        !/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(
                            uuid,
                        )
                    )
                        throw {
                            status: 400,
                        };
                    const reason = valueOf(form, `${target}-${kind}-reason`);
                    const effect = valueOf(form, "effect");
                    confirm({
                        label: mode,
                        object: uuid,
                        run: async () => {
                            const body =
                                mode === "revoke"
                                    ? {
                                          reason,
                                      }
                                    : {
                                          [kind === "roles"
                                              ? "roleUuids"
                                              : "permissionUuids"]: [uuid],
                                          reason,
                                          ...(kind === "permissions" &&
                                          target === "user"
                                              ? {
                                                    effect,
                                                }
                                              : {}),
                                      };
                            await api(
                                base + (mode === "revoke" ? "/" + uuid : ""),
                                {
                                    method:
                                        mode === "revoke" ? "DELETE" : "POST",
                                    body,
                                },
                            );
                            window.dispatchEvent(
                                new Event("iam-access-updated"),
                            );
                            notify(t("saved"));
                        },
                    });
                }}
            >
                <div className="form-grid grid grid-cols-2 gap-x-5 gap-y-3 max-[700px]:grid-cols-1 [.dashboard-root_&]:max-md:grid-cols-1">
                    <div>
                        <label htmlFor={modeId}>{t("action")}</label>
                        <select
                            id={modeId}
                            value={mode}
                            onChange={(e) => setMode(e.target.value)}
                        >
                            <option value="assign">{t("assign")}</option>
                            <option value="revoke">{t("revoke")}</option>
                        </select>
                    </div>
                    {can(
                        `${kind === "roles" ? "role" : "permission"}:read`,
                    ) && (
                        <AssignmentPicker
                            kind={kind}
                            field={kind === "roles" ? "roleId" : "permissionId"}
                        />
                    )}
                    <Input
                        name={kind === "roles" ? "roleId" : "permissionId"}
                        label={kind === "roles" ? "roleId" : "permissionId"}
                        required
                    />
                    <Input
                        name={`${target}-${kind}-reason`}
                        label="reason"
                        required
                    />
                    {target === "user" &&
                        kind === "permissions" &&
                        mode === "assign" && (
                            <label>
                                {t("effect")}
                                <FormSelect name="effect">
                                    <option value="GRANT">{t("allow")}</option>
                                    <option value="DENY">{t("deny")}</option>
                                </FormSelect>
                            </label>
                        )}
                </div>
            </ActionForm>
        </section>
    );
}
