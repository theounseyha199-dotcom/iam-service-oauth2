import { useWorkspace } from "@/context/workspace-context";
import { Input } from "@/components/ui/input";

export function ClientOptionGroup({
    field,
    error,
    options,
    onChange,
    children,
}) {
    const { t } = useWorkspace();
    const errorId = `w-${field.name}-error`;
    return (
        <fieldset className="min-w-0 space-y-3" aria-describedby={errorId}>
            <legend className="mb-2 text-sm font-medium">
                {t(field.name)}
            </legend>
            {options.map((option, index) => {
                const id = `w-${field.name}-${option.value}`;
                return (
                    <div key={option.value}>
                        <label
                            htmlFor={id}
                            className="flex cursor-pointer items-start gap-3 text-sm has-disabled:cursor-not-allowed has-disabled:text-muted-foreground"
                        >
                            <Input
                                id={id}
                                ref={index === 0 ? field.ref : undefined}
                                type="checkbox"
                                name={field.name}
                                value={option.value}
                                checked={field.value.includes(option.value)}
                                disabled={Boolean(option.disabledReason)}
                                onBlur={field.onBlur}
                                onChange={(event) =>
                                    onChange(
                                        event.target.checked
                                            ? [...field.value, option.value]
                                            : field.value.filter(
                                                  (value) =>
                                                      value !== option.value,
                                              ),
                                    )
                                }
                                aria-labelledby={`${id}-label`}
                                aria-describedby={`${id}-hint ${errorId}`}
                                aria-invalid={error ? "true" : undefined}
                                className="mt-0.5"
                            />
                            <span className="grid min-w-0 gap-1">
                                <span
                                    id={`${id}-label`}
                                    className="font-medium break-all"
                                >
                                    {option.label
                                        ? t(option.label)
                                        : option.value}
                                </span>
                                <span
                                    id={`${id}-hint`}
                                    className="text-xs text-muted-foreground"
                                >
                                    {t(
                                        option.disabledReason ||
                                            option.description ||
                                            "customScopeHint",
                                    )}
                                </span>
                            </span>
                        </label>
                    </div>
                );
            })}
            <p
                id={errorId}
                className="min-h-[1.3em] text-xs text-destructive"
                aria-live="polite"
            >
                {error
                    ? t(
                          error.message === "validation.required"
                              ? "required"
                              : "invalid",
                      )
                    : ""}
            </p>
            {children}
        </fieldset>
    );
}
