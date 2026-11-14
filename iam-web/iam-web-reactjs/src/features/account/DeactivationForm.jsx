import { reasonSchema } from "@/schemas/workspace";
import { useWorkspace } from "@/context/workspace-context";
import { ActionForm } from "@/components/workspace/ActionForm";
import { valueOf } from "@/lib/workspace-utils";
import { Input } from "@/components/workspace/WorkspaceInput";

export function DeactivationForm({ current, reload }) {
    const { t, api, confirm, notify, formatDate, setAppDirty } = useWorkspace();
    return (
        <>
            {current.status && (
                <section
                    aria-label={t("requestStatus")}
                    className="mt-5 rounded-xl border border-border bg-card p-5"
                >
                    <h2>
                        {t(current.pending ? "pendingReview" : current.status)}
                    </h2>
                    <p className="text-sm text-muted-foreground">
                        {t("requestDate")}: {formatDate(current.requested_at)}
                    </p>
                    <p className="my-3 wrap-anywhere">{current.reason}</p>
                    {current.review_note && (
                        <p className="my-3 wrap-anywhere">
                            {t("reviewReason")}: {current.review_note}
                        </p>
                    )}
                    {current.reviewed_at && (
                        <p>
                            {t("reviewedAt")}: {formatDate(current.reviewed_at)}
                        </p>
                    )}
                </section>
            )}
            <ActionForm
                schema={reasonSchema}
                deferred
                label={current.pending ? "updateRequest" : "deactivation"}
                onDirty={() => setAppDirty(true)}
                onSave={async (form) => {
                    const reason = valueOf(form, "reason");
                    confirm({
                        label: current.pending
                            ? "updateRequest"
                            : "deactivation",
                        object: reason,
                        description: t("deactivationHint"),
                        run: async () => {
                            await api("/deactivation", {
                                method: "POST",
                                body: {
                                    reason,
                                },
                            });
                            setAppDirty(false);
                            reload();
                            notify(t("requested"));
                        },
                    });
                }}
            >
                <Input
                    name="reason"
                    value={current.reason ?? ""}
                    maxLength={500}
                    required
                />
            </ActionForm>
        </>
    );
}
