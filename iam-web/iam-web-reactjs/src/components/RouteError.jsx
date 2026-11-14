import { workspaceText } from "@/workspace-messages";
import { Button } from "@/components/ui/button";
import { Notice } from "@/components/Notice";

export function RouteError() {
    const t = workspaceText(document.documentElement.lang);
    return (
        <main className="mx-auto max-w-xl p-6">
            <Notice message={t("error")} />
            <Button onClick={() => window.location.reload()}>
                {t("retry")}
            </Button>
        </main>
    );
}
