import { policySchema } from "@/schemas/workspace";
import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { useState, useEffect } from "react";
import { ResourceState } from "@/components/workspace/ResourceState";
import { ActionForm } from "@/components/workspace/ActionForm";
import { valueOf } from "@/lib/workspace-utils";
import { Input } from "@/components/workspace/WorkspaceInput";
import { FormCheckbox } from "@/components/forms/FormCheckbox";

export function Policy() {
    const { t, api, can, confirm, notify, setAppDirty } = useWorkspace();
    const r = useResource("/policy");
    const [dirty, setDirty] = useState(false);
    useEffect(() => {
        setAppDirty(dirty);
        return () => setAppDirty(false);
    }, [dirty, setAppDirty]);
    return (
        <>
            <p>{t("policyHint")}</p>
            <p className="hint text-sm text-muted-foreground [.page_&]:m-0! [.page_&]:rounded [.page_&]:border-s-2 [.page_&]:border-brand-200 [.page_&]:bg-slate-50 [.page_&]:px-4 [.page_&]:py-3 [.dashboard-root_&]:text-xs">
                {t("passwordPolicy")}
            </p>
            <ResourceState resource={r}>
                {(policy) => (
                    <ActionForm
                        schema={policySchema}
                        deferred
                        disabled={!can("policy:update")}
                        onDirty={() => setDirty(true)}
                        onSave={async (form) => {
                            const body = {
                                sessionMinutes: Number(
                                    valueOf(form, "sessionMinutes"),
                                ),
                                accessMinutes: Number(
                                    valueOf(form, "accessMinutes"),
                                ),
                                refreshMinutes: Number(
                                    valueOf(form, "refreshMinutes"),
                                ),
                                passwordMinimum: Number(
                                    valueOf(form, "passwordMinimum"),
                                ),
                                requireNumber:
                                    valueOf(form, "requireNumber") === "on",
                                requireUppercase:
                                    valueOf(form, "requireUppercase") === "on",
                                requireConsent:
                                    valueOf(form, "requireConsent") === "on",
                                version: policy.version,
                            };
                            confirm({
                                label: "save",
                                object: t("policy"),
                                run: async () => {
                                    await api("/policy", {
                                        method: "PUT",
                                        body,
                                    });
                                    setDirty(false);
                                    r.reload();
                                    notify(t("saved"));
                                },
                            });
                        }}
                    >
                        <Input
                            name="passwordMinimum"
                            value={policy.passwordMinimum}
                            type="number"
                            min={8}
                            max={64}
                            required
                        />
                        {[
                            "requireNumber",
                            "requireUppercase",
                            "requireConsent",
                        ].map((name) => (
                            <label
                                className="workspace-check flex items-center gap-[.65rem]"
                                key={name}
                            >
                                <FormCheckbox
                                    type="checkbox"
                                    name={name}
                                    defaultChecked={policy[name]}
                                />
                                {t(name)}
                            </label>
                        ))}
                        <Input
                            name="sessionMinutes"
                            value={policy.sessionMinutes}
                            type="number"
                            min={5}
                            max={1440}
                            required
                        />
                        <Input
                            name="accessMinutes"
                            value={policy.accessMinutes}
                            type="number"
                            min={1}
                            max={60}
                            required
                        />
                        <Input
                            name="refreshMinutes"
                            value={policy.refreshMinutes}
                            type="number"
                            min={5}
                            max={43200}
                            required
                        />
                    </ActionForm>
                )}
            </ResourceState>
        </>
    );
}
