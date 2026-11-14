import { Input } from "@/components/workspace/WorkspaceInput";
import { FormCheckbox } from "@/components/forms/FormCheckbox";
import { useWorkspace } from "@/context/workspace-context";
export function ClientFields({ row }) {
    const { t } = useWorkspace();
    return (
        <>
            <Input name="clientId" value={row.clientId} required />
            <Input name="clientName" value={row.clientName} required />
            <Input name="clientSecret" type="password" hint="secretHint" />
            <label className="workspace-check flex items-center gap-[.65rem]">
                <FormCheckbox
                    name="confidential"
                    type="checkbox"
                    defaultChecked={row.confidential ?? true}
                />
                {t("confidential")}
            </label>
            <Input
                name="grantTypes"
                value={
                    row.grantTypes?.join(", ") ??
                    "authorization_code, refresh_token"
                }
                required
                hint="commaHint"
            />
            <Input
                name="redirectUris"
                value={row.redirectUris?.join(", ")}
                hint="commaHint"
            />
            <Input
                name="scopes"
                value={row.scopes?.join(", ") ?? "openid, profile, email"}
                required
                hint="commaHint"
            />
            <Input
                name="accessMinutes"
                type="number"
                value={row.accessMinutes ?? 5}
                min={1}
                max={60}
                required
            />
            <Input
                name="refreshMinutes"
                type="number"
                value={row.refreshMinutes ?? 1440}
                min={5}
                max={43200}
                required
            />
        </>
    );
}
