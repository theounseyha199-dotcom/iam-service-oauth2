import { useRef, useState } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useWorkspace } from "@/context/workspace-context";
import { ErrorNotice } from "@/components/workspace/ErrorNotice";
import { Button } from "@/components/ui/button";

export function ActionForm({
    children,
    schema,
    onSave,
    label = "save",
    onDirty,
    disabled = false,
    deferred = false,
}) {
    const { t, notify } = useWorkspace();
    const methods = useForm({
        resolver: zodResolver(schema),
        mode: "onSubmit",
        reValidateMode: "onChange",
    });
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState(null);
    const running = useRef(false);
    async function save(form) {
        setBusy(true);
        setError(null);
        try {
            await onSave(form, methods);
            if (!deferred) notify(t("saved"));
        } catch (failure) {
            setError(failure);
            let first;
            for (const name of failure.fields ?? []) {
                if (!form.elements.namedItem(name)) continue;
                methods.setError(name, { type: "server", message: "invalid" });
                first ??= name;
            }
            if (first) methods.setFocus(first);
        } finally {
            running.current = false;
            setBusy(false);
        }
    }
    function submit(event) {
        event.preventDefault();
        if (running.current || disabled || event.nativeEvent.isComposing)
            return;
        running.current = true;
        const form = event.currentTarget;
        void methods.handleSubmit(
            () => save(form),
            () => {
                running.current = false;
            },
        )(event);
    }
    return (
        <FormProvider {...methods}>
            <form
                noValidate
                onSubmit={submit}
                onInput={onDirty}
                aria-busy={busy}
            >
                <ErrorNotice error={error} />
                <fieldset disabled={busy || disabled}>
                    {children}
                    <Button type="submit" disabled={busy || disabled}>
                        {t(label)}
                    </Button>
                </fieldset>
            </form>
        </FormProvider>
    );
}
