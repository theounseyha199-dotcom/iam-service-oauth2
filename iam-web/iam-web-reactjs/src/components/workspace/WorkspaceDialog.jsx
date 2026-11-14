import { useWorkspace } from "@/context/workspace-context";
import { Button } from "@/components/ui/button";
import { ErrorNotice } from "@/components/workspace/ErrorNotice";
export function WorkspaceDialog({
    pending,
    busy,
    error,
    dialogRef,
    onCancel,
    onCommit,
}) {
    const { t } = useWorkspace();
    return (
        <dialog
            ref={dialogRef}
            className="workspace-dialog m-auto w-[calc(100%-2rem)] max-w-[540px] rounded-xl border border-border bg-card p-8 text-foreground wrap-anywhere backdrop:bg-slate-900/45 [&_h2]:m-0 [&_h2]:text-[1.2rem] [&_h2]:font-semibold [&_p]:my-4 [&_p]:text-[.85rem] [&_p]:text-slate-600"
            aria-labelledby="workspace-confirm-title"
            aria-describedby="workspace-confirm-description"
            onCancel={(e) => {
                e.preventDefault();
                onCancel();
            }}
        >
            <h2 id="workspace-confirm-title">
                {t(pending?.label ?? "confirm")}
            </h2>
            <p>{pending?.object}</p>
            <p id="workspace-confirm-description">
                {pending?.description ?? t("consequence")}
            </p>
            <ErrorNotice error={error} />
            <div className="actions mt-5 flex flex-wrap items-end justify-end gap-3 [&_form]:m-0! [.dashboard-root_&]:items-center [.dashboard-root_&]:gap-[.65rem]">
                <Button variant="outline" disabled={busy} onClick={onCancel}>
                    {t("cancel")}
                </Button>
                <Button
                    variant="destructive"
                    disabled={busy}
                    onClick={onCommit}
                >
                    {t(pending?.label ?? "confirm")}
                </Button>
            </div>
        </dialog>
    );
}
