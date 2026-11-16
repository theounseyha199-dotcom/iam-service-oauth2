import { useState } from "react";
import { useFormContext, useWatch } from "react-hook-form";
import { Input } from "@/components/workspace/WorkspaceInput";
import { Button } from "@/components/ui/button";
import { useWorkspace } from "@/context/workspace-context";
import { generateClientSecret } from "@/lib/client-secret";

export function ClientSecretField() {
    const { t } = useWorkspace();
    const { setValue } = useFormContext();
    const value = useWatch({ name: "clientSecret", defaultValue: "" });
    const [feedback, setFeedback] = useState({ message: "", value: "" });
    const message = feedback.value === value ? feedback.message : "";

    function generate(event) {
        try {
            const secret = generateClientSecret();
            setValue("clientSecret", secret, {
                shouldDirty: true,
                shouldTouch: true,
                shouldValidate: true,
            });
            // Programmatic values must also notify the form's unsaved-changes guard.
            event.currentTarget.form?.dispatchEvent(
                new Event("input", { bubbles: true }),
            );
            setFeedback({ message: "secretGenerated", value: secret });
        } catch {
            setFeedback({ message: "secretGenerationUnavailable", value });
        }
    }

    async function copy() {
        try {
            await navigator.clipboard.writeText(value);
            setFeedback({ message: "secretCopied", value });
        } catch {
            setFeedback({ message: "secretCopyUnavailable", value });
        }
    }

    return (
        <Input
            name="clientSecret"
            type="password"
            hint="secretHint"
            actions={
                <>
                    <Button type="button" variant="outline" onClick={generate}>
                        {t("generateSecret")}
                    </Button>
                    <Button
                        type="button"
                        variant="outline"
                        onClick={copy}
                        disabled={!value}
                    >
                        {t("copySecret")}
                    </Button>
                    <span
                        role="status"
                        aria-atomic="true"
                        className={`min-h-12 basis-full text-xs ${message.endsWith("Unavailable") ? "text-destructive" : "text-muted-foreground"}`}
                    >
                        {message ? t(message) : ""}
                    </span>
                </>
            }
        />
    );
}
