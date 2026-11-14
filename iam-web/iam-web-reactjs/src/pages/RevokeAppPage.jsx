import { Confirmation } from "@/components/account/Confirmation";
import { usePage } from "@/context/page-context";

export function RevokeAppPage() {
    const { data: d } = usePage();
    return (
        <Confirmation
            object={d.app?.name}
            warning="apps.warning"
            action={`/profile/apps/${encodeURIComponent(d.app?.id)}/revoke`}
            cancel="/profile/apps"
            label="apps.revoke"
        />
    );
}
