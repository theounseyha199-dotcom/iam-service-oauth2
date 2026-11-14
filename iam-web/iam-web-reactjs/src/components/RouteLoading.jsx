import { workspaceText } from "@/workspace-messages";
export function RouteLoading() {
    return (
        <div
            role="status"
            className="min-h-40 p-8 text-sm text-muted-foreground"
        >
            {workspaceText(document.documentElement.lang)("loading")}
        </div>
    );
}
