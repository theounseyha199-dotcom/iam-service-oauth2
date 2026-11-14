import { useWorkspace } from "@/context/workspace-context";
import { ErrorNotice } from "@/components/workspace/ErrorNotice";
import { Button } from "@/components/ui/button";

export function ResourceState({ resource, children }) {
    const { t } = useWorkspace();
    if (resource.loading)
        return (
            <div
                className="workspace-state min-h-40 py-8 text-[.8rem] text-slate-500"
                role="status"
            >
                {t("loading")}
            </div>
        );
    if (resource.error)
        return (
            <div className="workspace-state min-h-40 py-8 text-[.8rem] text-slate-500">
                <ErrorNotice error={resource.error} />
                <Button variant="outline" onClick={resource.reload}>
                    {t("retry")}
                </Button>
            </div>
        );
    return children(resource.data);
}
