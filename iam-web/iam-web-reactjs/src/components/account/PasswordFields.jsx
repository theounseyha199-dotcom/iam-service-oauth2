import { usePage } from "@/context/page-context";
import { Field } from "@/components/account/Field";

export function PasswordFields({ current = false, reset = false }) {
    const { t } = usePage();
    return (
        <>
            <p className="hint text-sm text-muted-foreground [.page_&]:m-0! [.page_&]:rounded [.page_&]:border-s-2 [.page_&]:border-brand-200 [.page_&]:bg-slate-50 [.page_&]:px-4 [.page_&]:py-3 [.dashboard-root_&]:text-xs">
                {t("password.hint")}
            </p>
            {current && (
                <Field
                    name="currentPassword"
                    type="password"
                    required
                    autoComplete="current-password"
                />
            )}
            <Field
                name={current || reset ? "newPassword" : "password"}
                type="password"
                required
                maxLength={reset ? 72 : 1000}
            />
            <Field
                name="confirmPassword"
                type="password"
                required
                maxLength={reset ? 72 : 1000}
            />
        </>
    );
}
