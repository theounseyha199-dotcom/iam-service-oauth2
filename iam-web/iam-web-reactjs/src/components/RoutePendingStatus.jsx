import { useNavigation } from "react-router";
import { usePage } from "@/context/page-context";
import { workspaceText } from "@/workspace-messages";

export function RoutePendingStatus() {
    const pending = useNavigation().state === "loading";
    const { language } = usePage();
    return pending ? (
        <div
            role="status"
            className="fixed right-4 bottom-4 z-50 rounded-lg border border-border bg-card px-4 py-3 text-sm text-muted-foreground shadow-sm"
        >
            {workspaceText(language)("loading")}
        </div>
    ) : null;
}
