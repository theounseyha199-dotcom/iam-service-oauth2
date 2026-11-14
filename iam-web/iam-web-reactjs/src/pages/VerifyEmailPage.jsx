import { Notice } from "@/components/Notice";
import { Form } from "@/components/account/AccountForm";
import { Field } from "@/components/account/Field";
import { Submit } from "@/components/account/Submit";
import { Link } from "@/components/account/AccountLink";
import { BackToLogin } from "@/components/account/BackToLogin";
import { usePage } from "@/context/page-context";

export function VerifyEmailPage() {
    const { data: d, t } = usePage();
    return (
        <>
            {d.done ? (
                <Notice success message={t("verification.done")} />
            ) : (
                <>
                    <p>{t("verification.confirm")}</p>
                    <Form noValidate action="/verify-email">
                        <Field
                            name="email"
                            type="email"
                            required
                            value={d.email}
                        />
                        <Field
                            name="code"
                            type="password"
                            required
                            inputMode="numeric"
                            autoComplete="one-time-code"
                            maxLength={6}
                            error={
                                d.errorCode === "invalid_verification_code"
                                    ? "verification.codeError"
                                    : ""
                            }
                        />
                        <Submit label="verification.title" />
                    </Form>
                    <p className="hint text-sm text-muted-foreground [.page_&]:m-0! [.page_&]:rounded [.page_&]:border-s-2 [.page_&]:border-brand-200 [.page_&]:bg-slate-50 [.page_&]:px-4 [.page_&]:py-3 [.dashboard-root_&]:text-xs">
                        {t("verification.codeHint")}
                    </p>
                    <p>
                        <Link
                            to="/resend-verification"
                            label="verification.resend"
                        />
                    </p>
                </>
            )}
            <BackToLogin />
        </>
    );
}
