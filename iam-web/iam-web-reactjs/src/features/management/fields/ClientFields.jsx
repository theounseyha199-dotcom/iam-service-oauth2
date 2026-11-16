import { useRef, useState } from "react";
import { useController } from "react-hook-form";
import { Input } from "@/components/workspace/WorkspaceInput";
import { Input as ChoiceInput } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useWorkspace } from "@/context/workspace-context";
import { ClientOptionGroup } from "@/features/management/fields/ClientOptionGroup";
import { ClientSecretField } from "@/features/management/fields/ClientSecretField";

const standardScopes = ["openid", "profile", "email", "phone", "address"];

export function ClientFields({ row }) {
    const { t } = useWorkspace();
    const { field: confidential } = useController({
        name: "confidential",
        defaultValue: row.confidential ?? true,
    });
    const { field: grants, fieldState: grantState } = useController({
        name: "grantTypes",
        defaultValue: row.grantTypes ?? ["authorization_code", "refresh_token"],
    });
    const { field: scopes, fieldState: scopeState } = useController({
        name: "scopes",
        defaultValue: row.scopes ?? ["openid", "profile", "email"],
    });
    const [customScopes, setCustomScopes] = useState(() =>
        (row.scopes ?? []).filter((scope) => !standardScopes.includes(scope)),
    );
    const [draft, setDraft] = useState("");
    const [scopeError, setScopeError] = useState("");
    const customInput = useRef(null);

    function changeGrants(values) {
        grants.onChange(
            values.includes("authorization_code")
                ? values
                : values.filter((value) => value !== "refresh_token"),
        );
    }

    function addScope() {
        const scope = draft.trim();
        let error = "";
        if (!/^[A-Za-z0-9_.:-]{1,100}$/.test(scope)) error = "scopeFormatHint";
        else if ([...standardScopes, ...customScopes].includes(scope))
            error = "scopeExists";
        else if (scopes.value.length >= 30) error = "scopeLimit";
        setScopeError(error);
        if (!error) {
            setCustomScopes((values) => [...values, scope]);
            scopes.onChange([...scopes.value, scope]);
            setDraft("");
        }
        customInput.current?.focus();
    }

    return (
        <>
            <Input name="clientId" value={row.clientId} required />
            <Input name="clientName" value={row.clientName} required />
            <div className="col-span-full grid grid-cols-2 items-start gap-5 max-md:grid-cols-1">
                <label className="workspace-check flex items-center gap-[.65rem]">
                    <ChoiceInput
                        {...confidential}
                        value="on"
                        type="checkbox"
                        checked={confidential.value}
                        onChange={(event) => {
                            confidential.onChange(event.target.checked);
                            if (!event.target.checked)
                                grants.onChange(
                                    grants.value.filter(
                                        (value) =>
                                            value !== "client_credentials",
                                    ),
                                );
                        }}
                    />
                    {t("confidential")}
                </label>
                {confidential.value ? (
                    <ClientSecretField />
                ) : (
                    <p className="text-sm text-muted-foreground">
                        {t("publicApplicationHint")}
                    </p>
                )}
            </div>
            <ClientOptionGroup
                field={grants}
                error={grantState.error}
                onChange={changeGrants}
                options={[
                    {
                        value: "authorization_code",
                        label: "authorizationCode",
                        description: "authorizationCodeHint",
                    },
                    {
                        value: "refresh_token",
                        label: "refreshToken",
                        description: "refreshTokenHint",
                        disabledReason: !grants.value.includes(
                            "authorization_code",
                        )
                            ? "refreshRequiresCode"
                            : null,
                    },
                    {
                        value: "client_credentials",
                        label: "clientCredentials",
                        description: "clientCredentialsHint",
                        disabledReason: !confidential.value
                            ? "credentialsRequireConfidential"
                            : null,
                    },
                ]}
            />
            <ClientOptionGroup
                field={scopes}
                error={scopeState.error}
                onChange={scopes.onChange}
                options={[
                    ...standardScopes.map((value) => ({
                        value,
                        description: `scopeHint_${value}`,
                    })),
                    ...customScopes.map((value) => ({ value })),
                ]}
            >
                <div className="space-y-2">
                    <label
                        htmlFor="w-customScope"
                        className="block text-sm font-medium"
                    >
                        {t("customScope")}
                    </label>
                    <div className="flex flex-wrap items-start gap-2">
                        <ChoiceInput
                            id="w-customScope"
                            placeholder={t("placeholders.customScope")}
                            ref={customInput}
                            value={draft}
                            onChange={(event) => {
                                setDraft(event.target.value);
                                setScopeError("");
                            }}
                            onKeyDown={(event) => {
                                if (
                                    event.key === "Enter" &&
                                    !event.nativeEvent.isComposing
                                ) {
                                    event.preventDefault();
                                    addScope();
                                }
                            }}
                            maxLength={100}
                            autoComplete="off"
                            aria-invalid={scopeError ? "true" : undefined}
                            aria-describedby="w-customScope-hint w-customScope-error"
                            className="min-w-0 flex-1 basis-36"
                        />
                        <Button
                            type="button"
                            variant="outline"
                            onClick={addScope}
                            disabled={!draft.trim()}
                        >
                            {t("addCustomScope")}
                        </Button>
                    </div>
                    <p
                        id="w-customScope-hint"
                        className="text-xs text-muted-foreground"
                    >
                        {t("scopeFormatHint")}
                    </p>
                    <p
                        id="w-customScope-error"
                        className="min-h-[1.3em] text-xs text-destructive"
                        aria-live="polite"
                    >
                        {scopeError ? t(scopeError) : ""}
                    </p>
                </div>
            </ClientOptionGroup>
            <div className="col-span-full">
                <Input
                    name="redirectUris"
                    value={row.redirectUris?.join(", ")}
                    hint="commaHint"
                />
            </div>
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
