import { useWorkspace } from "@/context/workspace-context";
import { ActionForm } from "@/components/workspace/ActionForm";
import { Input } from "@/components/workspace/WorkspaceInput";
import { Button } from "@/components/ui/button";
import { reviewSchema } from "@/schemas/workspace";
import { valueOf } from "@/lib/workspace-utils";

export function ReviewDeactivation({ request, decision, onDone, onCancel }) {
    const { t, api, confirm, notify } = useWorkspace();
    const label = decision === "APPROVED" ? "approve" : "reject";
    return (
        <section
            className="mt-5 rounded-lg border border-border p-5"
            aria-label={t("reviewRequest")}
        >
            <h3>
                {t(label)} · {request.username}
            </h3>
            <p className="my-3 wrap-anywhere">{request.reason}</p>
            <ActionForm
                schema={reviewSchema}
                deferred
                label={label}
                onSave={async (form) => {
                    const reason = valueOf(form, "reviewReason").trim();
                    confirm({
                        label,
                        object: request.username,
                        description: t(
                            decision === "APPROVED"
                                ? "approveWarning"
                                : "rejectWarning",
                        ),
                        run: async () => {
                            await api(
                                `/deactivation/${request.subject}/review`,
                                {
                                    method: "POST",
                                    body: {
                                        decision,
                                        reason,
                                        version: request.version,
                                    },
                                },
                            );
                            onDone();
                            notify(t("reviewSaved"));
                        },
                    });
                }}
            >
                <Input name="reviewReason" required />
            </ActionForm>
            <Button className="mt-3" variant="outline" onClick={onCancel}>
                {t("cancel")}
            </Button>
        </section>
    );
}
