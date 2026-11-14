import { passwordChange } from "@/schemas/workspace";
import { useWorkspace } from "@/context/workspace-context";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Sessions } from "@/features/account/Sessions";
import { Activity } from "@/features/account/Activity";
import { UserAccess } from "@/features/management/UserAccess";
import { Assignment } from "@/features/management/Assignment";
import { ActionForm } from "@/components/workspace/ActionForm";
import { valueOf } from "@/lib/workspace-utils";
import { Input } from "@/components/workspace/WorkspaceInput";
import { UserAccountStatus } from "@/features/management/UserAccountStatus";

export function UserTools({ id, row, reload }) {
    const { t, can, api, confirm, notify } = useWorkspace();
    const [tab, setTab] = useState("access");
    return (
        <section className="workspace-section mt-6">
            <UserAccountStatus user={row} />
            <div className="workspace-toolbar my-4 flex flex-wrap items-end justify-between gap-4 [&_form]:m-0 [&_form]:max-w-[380px] [&_form]:flex-1 [&_select]:mt-[.4rem]">
                <h2>{t("security")}</h2>
                <div className="actions mt-5 flex flex-wrap items-end justify-end gap-3 [&_form]:m-0! [.dashboard-root_&]:items-center [.dashboard-root_&]:gap-[.65rem]">
                    {can("user:update") &&
                        [
                            row.enabled ? "disable" : "enable",
                            row.accountNonLocked ? "lock" : "unlock",
                        ].map((action) => (
                            <Button
                                key={action}
                                variant="outline"
                                onClick={() =>
                                    confirm({
                                        label: action,
                                        object: row.username,
                                        run: async () => {
                                            await api(
                                                `/users/${id}/${action}`,
                                                {
                                                    method: "POST",
                                                },
                                            );
                                            reload();
                                            notify(t("saved"));
                                        },
                                    })
                                }
                            >
                                {t(action)}
                            </Button>
                        ))}
                </div>
            </div>
            <div
                className="workspace-tabs my-4 flex flex-wrap gap-2 [&_[aria-pressed=true]]:bg-accent [&_[aria-pressed=true]]:border-primary"
                role="group"
                aria-label={t("security")}
            >
                {["access", "sessions", "history"].map((key) => (
                    <Button
                        variant="outline"
                        aria-pressed={key === tab}
                        key={key}
                        onClick={() => setTab(key)}
                    >
                        {t(key)}
                    </Button>
                ))}
            </div>
            {tab === "sessions" ? (
                <Sessions user={id} />
            ) : tab === "history" ? (
                <Activity history user={id} />
            ) : (
                <>
                    <UserAccess id={id} />
                    {can("user:assign") && (
                        <>
                            <Assignment id={id} kind="roles" />
                            <Assignment id={id} kind="permissions" />
                        </>
                    )}
                    {can("user:update") && (
                        <section className="card block min-w-0 rounded-2xl border border-border bg-card p-[clamp(1.25rem,4vw,2rem)] shadow-none [.dashboard-root_&]:rounded-[10px] [.dashboard-root_&]:p-[1.4rem] workspace-section mt-6">
                            <h3>{t("reset")}</h3>
                            <ActionForm
                                schema={passwordChange}
                                deferred
                                label="reset"
                                onSave={async (form, formMethods) => {
                                    const password = valueOf(
                                        form,
                                        "newPassword",
                                    );
                                    const confirmation = valueOf(
                                        form,
                                        "confirmPassword",
                                    );
                                    if (password !== confirmation)
                                        throw {
                                            status: 400,
                                        };
                                    confirm({
                                        label: "reset",
                                        object: row.username,
                                        run: async () => {
                                            await api(`/users/${id}/password`, {
                                                method: "PUT",
                                                body: {
                                                    newPassword: password,
                                                    confirmPassword:
                                                        confirmation,
                                                },
                                            });
                                            formMethods.reset({
                                                newPassword: "",
                                                confirmPassword: "",
                                            });
                                            notify(t("saved"));
                                        },
                                    });
                                }}
                            >
                                <Input
                                    name="newPassword"
                                    type="password"
                                    required
                                    hint="passwordHint"
                                />
                                <Input
                                    name="confirmPassword"
                                    type="password"
                                    required
                                />
                            </ActionForm>
                        </section>
                    )}
                </>
            )}
        </section>
    );
}
