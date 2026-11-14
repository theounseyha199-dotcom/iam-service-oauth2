import { useFormField } from "@/hooks/use-form-field";
import { usePage } from "@/context/page-context";

export function SelectField({ name, choices }) {
    const { t, data } = usePage();
    const { registration, error } = useFormField(name, data.errors?.[name]);
    return (
        <div className="field m-0 min-w-0 [&>label]:mb-2 [&>label]:block [&>label]:text-sm [&>label]:font-semibold [&>label]:text-foreground [.dashboard-root_&>label]:mb-[.4rem] [.dashboard-root_&>label]:text-[.78rem] [.dashboard-root_&>label]:font-medium">
            <label htmlFor={name}>{t(`field.${name}`)}</label>
            <select
                {...registration}
                id={name}
                name={name}
                defaultValue={
                    data.values?.[name] ?? (name === "locale" ? "en" : "")
                }
                aria-invalid={error ? "true" : undefined}
                aria-describedby={`${name}-error`}
            >
                {choices.map(([value, key]) => (
                    <option key={value} value={value}>
                        {t(key)}
                    </option>
                ))}
            </select>
            <p
                id={`${name}-error`}
                className="field-error mt-1! mb-0! min-h-[1.3em] text-[.8125rem] text-destructive [.dashboard-root_&]:min-h-[1.15em] [.dashboard-root_&]:text-[.7rem]"
            >
                {error ? t(error) : ""}
            </p>
        </div>
    );
}
