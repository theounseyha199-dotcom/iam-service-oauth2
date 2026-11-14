import { useWorkspace } from "@/context/workspace-context";
import { ActionForm } from "@/components/workspace/ActionForm";
import { Input } from "@/components/workspace/WorkspaceInput";
import { FormCheckbox } from "@/components/forms/FormCheckbox";
import { Button } from "@/components/ui/button";
import { activityFilterSchema } from "@/schemas/workspace";
import { valueOf } from "@/lib/workspace-utils";

export function ActivityFilters({ audit }) {
    const { t, state, setQuery } = useWorkspace();
    return (
        <section
            className="my-4 rounded-lg border border-border p-4"
            aria-label={t("filterActivity")}
        >
            <p className="mb-3 text-sm text-muted-foreground">
                {t("activityFilterHint")}
            </p>
            <ActionForm
                key={[
                    state.actor,
                    state.target,
                    state.from,
                    state.to,
                    state.suspicious,
                ].join("|")}
                schema={activityFilterSchema}
                deferred
                label="applyFilters"
                onSave={async (form) =>
                    setQuery({
                        ...(audit
                            ? {
                                  actor: valueOf(form, "actor"),
                                  target: valueOf(form, "target"),
                              }
                            : {}),
                        from: valueOf(form, "from"),
                        to: valueOf(form, "to"),
                        suspicious:
                            valueOf(form, "suspicious") === "on" ? "true" : "",
                        page: 0,
                    })
                }
            >
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                    {audit && (
                        <>
                            <Input name="actor" value={state.actor} />
                            <Input name="target" value={state.target} />
                        </>
                    )}
                    <Input name="from" type="date" value={state.from} />
                    <Input name="to" type="date" value={state.to} />
                </div>
                <label className="my-3 flex items-center gap-2">
                    <FormCheckbox
                        name="suspicious"
                        type="checkbox"
                        defaultChecked={state.suspicious === "true"}
                    />
                    {t("suspiciousOnly")}
                </label>
            </ActionForm>
            <Button
                className="mt-3"
                variant="outline"
                onClick={() =>
                    setQuery({
                        actor: "",
                        target: "",
                        from: "",
                        to: "",
                        suspicious: "",
                        page: 0,
                    })
                }
            >
                {t("clearFilters")}
            </Button>
        </section>
    );
}
