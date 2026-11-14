import { submitNativeForm } from "@/lib/submit-native-form";
import { useEffect, useRef, useState } from "react";
import { flushSync } from "react-dom";
import { FormProvider, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { usePage } from "@/context/page-context";
import { FormBusyContext } from "@/context/form-busy-context";
import { Hidden } from "@/components/account/Hidden";
import { accountSchema } from "@/schemas/account";

export function Form({ action, children, dirty = false, className, ...props }) {
    const { url, csrfName, csrfToken, data, setDirty } = usePage();
    const methods = useForm({
        resolver: zodResolver(accountSchema(action, data)),
        defaultValues: data.values ?? {},
        mode: "onSubmit",
        reValidateMode: "onChange",
    });
    const [busy, setBusy] = useState(false);
    const running = useRef(false);
    useEffect(() => {
        const reset = () => {
            running.current = false;
            setBusy(false);
        };
        window.addEventListener("pageshow", reset);
        return () => window.removeEventListener("pageshow", reset);
    }, []);
    function submit(event) {
        event.preventDefault();
        if (running.current || event.nativeEvent.isComposing) return;
        running.current = true;
        const form = event.currentTarget;
        const submitter = event.nativeEvent.submitter;
        void methods.handleSubmit(
            () => {
                flushSync(() => setDirty(false));
                setBusy(true);
                submitNativeForm(form, submitter);
            },
            () => {
                for (const name of [
                    "password",
                    "currentPassword",
                    "newPassword",
                    "confirmPassword",
                    "code",
                ]) {
                    if (name in methods.getValues())
                        methods.resetField(name, {
                            defaultValue: "",
                            keepError: true,
                        });
                }
                running.current = false;
            },
        )(event);
    }
    return (
        <FormProvider {...methods}>
            <form
                method="post"
                action={url(action)}
                noValidate
                className={className}
                aria-busy={busy}
                onSubmit={submit}
                onInput={() => {
                    if (dirty) setDirty(true);
                }}
                {...props}
            >
                <Hidden name={csrfName} value={csrfToken} />
                <FormBusyContext.Provider value={busy}>
                    <fieldset
                        className="grid min-w-0 gap-[1.125rem] border-0 p-0 disabled:opacity-70"
                        data-busy={busy}
                    >
                        {children}
                    </fieldset>
                </FormBusyContext.Provider>
            </form>
        </FormProvider>
    );
}
