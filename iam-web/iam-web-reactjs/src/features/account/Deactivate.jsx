import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { ResourceState } from "@/components/workspace/ResourceState";
import { DeactivationForm } from "@/features/account/DeactivationForm";

export function Deactivate() {
    const { t } = useWorkspace();
    const request = useResource("/deactivation/mine");
    return (
        <>
            <p>{t("deactivationHint")}</p>
            <ResourceState resource={request}>
                {(current) => (
                    <DeactivationForm
                        current={current}
                        reload={request.reload}
                    />
                )}
            </ResourceState>
        </>
    );
}
