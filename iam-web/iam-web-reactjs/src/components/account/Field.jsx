import { Eye, EyeOff } from "lucide-react";
import { useFormField } from "@/hooks/use-form-field";
import { usePage } from "@/context/page-context";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export function Field({
    name,
    label,
    type = "text",
    required = false,
    autoComplete,
    maxLength,
    inputMode,
    value,
    error,
    onValueChange,
}) {
    const { t, data } = usePage();
    const [show, setShow] = useState(false);
    const password = type === "password";
    const { registration, error: errorCode } = useFormField(
        name,
        error ?? data.errors?.[name] ?? data.fieldErrors?.[name],
    );
    const input = (
        <Input
            {...registration}
            onChange={(event) => {
                void registration.onChange(event);
                onValueChange?.(event.target.value);
            }}
            density="account"
            id={name}
            name={name}
            type={password && show ? "text" : type}
            defaultValue={password ? "" : (value ?? data.values?.[name] ?? "")}
            required={required}
            maxLength={maxLength ?? (password ? 1000 : 255)}
            inputMode={inputMode}
            autoComplete={
                autoComplete ??
                (password
                    ? "new-password"
                    : name === "givenName"
                      ? "given-name"
                      : name === "familyName"
                        ? "family-name"
                        : name === "email"
                          ? "email"
                          : undefined)
            }
            aria-invalid={errorCode ? "true" : undefined}
            aria-describedby={`${name}-error`}
            autoCapitalize={name === "username" ? "none" : undefined}
            spellCheck={name === "username" ? false : undefined}
        />
    );
    return (
        <div className="field m-0 min-w-0 [&>label]:mb-2 [&>label]:block [&>label]:text-sm [&>label]:font-semibold [&>label]:text-foreground [.dashboard-root_&>label]:mb-[.4rem] [.dashboard-root_&>label]:text-[.78rem] [.dashboard-root_&>label]:font-medium">
            <label htmlFor={name}>
                {t(label ?? `field.${name}`)}{" "}
                {!required && (
                    <span className="muted text-muted-foreground [label_&]:ms-[.375rem] [label_&]:text-xs [label_&]:font-normal">
                        {t("common.optional")}
                    </span>
                )}
            </label>
            {password ? (
                <div className="input-row flex items-stretch gap-2 [&_input]:flex-1 [.dashboard-root_&]:items-center">
                    {input}
                    <Button
                        type="button"
                        variant="outline"
                        size="account"
                        className="secondary border-primary bg-card text-brand-800 hover:bg-accent active:bg-brand-100 password-toggle static h-auto w-20 min-w-20 shrink-0 border-input text-[.8125rem] text-slate-600 hover:border-primary hover:text-brand-800 hidden:hidden"
                        aria-label={t(
                            show
                                ? name === "code"
                                    ? "common.hideCode"
                                    : "common.hidePassword"
                                : name === "code"
                                  ? "common.showCode"
                                  : "common.showPassword",
                        )}
                        aria-pressed={show}
                        onClick={() => setShow(!show)}
                    >
                        {show ? (
                            <EyeOff aria-hidden="true" />
                        ) : (
                            <Eye aria-hidden="true" />
                        )}
                        {t(show ? "common.hide" : "common.show")}
                    </Button>
                </div>
            ) : (
                input
            )}
            <p
                id={`${name}-error`}
                className="field-error mt-1! mb-0! min-h-[1.3em] text-[.8125rem] text-destructive [.dashboard-root_&]:min-h-[1.15em] [.dashboard-root_&]:text-[.7rem]"
                aria-live="polite"
            >
                {errorCode ? t(errorCode) : ""}
            </p>
        </div>
    );
}
