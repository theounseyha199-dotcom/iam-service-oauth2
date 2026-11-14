import { Confirmation } from "@/components/account/Confirmation";
import { usePage } from "@/context/page-context";

export function UnlinkAccountPage() {
    const { data: d, t } = usePage();
    return (
        <Confirmation
            object={t(`provider.${d.identity?.provider}`)}
            warning="accounts.unlinkWarning"
            action={`/profile/accounts/${d.identity?.uuid}/unlink`}
            cancel="/profile/accounts"
            label="accounts.unlink"
        />
    );
}
