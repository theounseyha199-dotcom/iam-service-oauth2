import { Eye, EyeOff } from "lucide-react";
import { useFormField } from "@/hooks/use-form-field";
import { useWorkspace } from "@/context/workspace-context";
import { useState } from "react";
import { Input as ShadcnInput } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export function Input({
    name,
    label,
    type = "text",
    value = "",
    required = false,
    hint,
    min,
    max,
    readOnly = false,
}) {
    const { t } = useWorkspace();
    const [show, setShow] = useState(false);
    const { registration, error } = useFormField(name);
    return (
        <div className="field m-0 min-w-0 [&>label]:mb-2 [&>label]:block [&>label]:text-sm [&>label]:font-semibold [&>label]:text-foreground [.dashboard-root_&>label]:mb-[.4rem] [.dashboard-root_&>label]:text-[.78rem] [.dashboard-root_&>label]:font-medium">
            <label htmlFor={`w-${name}`}>{t(label ?? name)}</label>
            <div
                className={
                    type === "password"
                        ? "input-row flex items-stretch gap-2 [&_input]:flex-1 [.dashboard-root_&]:items-center"
                        : undefined
                }
            >
                <ShadcnInput
                    {...registration}
                    aria-invalid={error ? "true" : undefined}
                    id={`w-${name}`}
                    name={name}
                    type={type === "password" && show ? "text" : type}
                    defaultValue={value ?? ""}
                    required={required}
                    min={min}
                    max={max}
                    maxLength={type === "password" ? 255 : 500}
                    readOnly={readOnly}
                    autoComplete={
                        type === "password" ? "new-password" : undefined
                    }
                    aria-describedby={[
                        `w-${name}-error`,
                        hint && `w-${name}-hint`,
                    ]
                        .filter(Boolean)
                        .join(" ")}
                />
                {type === "password" && (
                    <Button
                        type="button"
                        variant="outline"
                        aria-pressed={show}
                        aria-label={`${t(show ? "hide" : "show")} ${t(label ?? name)}`}
                        onClick={() => setShow((v) => !v)}
                    >
                        {show ? (
                            <EyeOff aria-hidden="true" />
                        ) : (
                            <Eye aria-hidden="true" />
                        )}
                        {t(show ? "hide" : "show")}
                    </Button>
                )}
            </div>
            {hint && (
                <p
                    className="hint text-sm text-muted-foreground [.page_&]:m-0! [.page_&]:rounded [.page_&]:border-s-2 [.page_&]:border-brand-200 [.page_&]:bg-slate-50 [.page_&]:px-4 [.page_&]:py-3 [.dashboard-root_&]:text-xs"
                    id={`w-${name}-hint`}
                >
                    {t(hint)}
                </p>
            )}
            <p
                className="field-error mt-1! mb-0! min-h-[1.3em] text-[.8125rem] text-destructive [.dashboard-root_&]:min-h-[1.15em] [.dashboard-root_&]:text-[.7rem]"
                id={`w-${name}-error`}
                aria-live="polite"
            >
                {error
                    ? t(
                          error === "validation.required"
                              ? "required"
                              : "invalid",
                      )
                    : ""}
            </p>
        </div>
    );
}
