import { Notice } from "@/components/Notice";
import { Form } from "@/components/account/AccountForm";
import { Field } from "@/components/account/Field";
import { Submit } from "@/components/account/Submit";
import { Link } from "@/components/account/AccountLink";
import { Providers } from "@/components/account/Providers";
import { usePage } from "@/context/page-context";

export function LoginPage() {
    const { data: d, t } = usePage();
    return (
        <>
            <p className="eyebrow mb-2! mt-0! text-xs font-bold tracking-[.1em] text-brand-800">
                <span
                    className="identity-mark inline-flex size-8 items-center justify-center rounded-lg bg-primary text-xl font-extrabold text-white"
                    aria-hidden="true"
                >
                    I
                </span>
                {t("common.brand")}
            </p>
            <h1>{t("login.title")}</h1>
            <p className="intro max-w-[42ch] text-muted-foreground">
                {t("login.intro")}
            </p>
            <Notice message={d.loginError && t("login.error")} />
            <Notice message={d.oauth2Error && t("login.providerError")} />
            <Notice success message={d.loggedOut && t("login.logout")} />
            <Notice success message={d.registered && t("login.registered")} />
            <Form noValidate action="/login">
                <Field
                    name="username"
                    label="login.username"
                    required
                    autoComplete="username"
                />
                <Field
                    name="password"
                    type="password"
                    required
                    autoComplete="current-password"
                />
                <Submit label="login.title" />
            </Form>
            <p>
                <Link to="/forgot-password" label="recovery.title" />
            </p>
            <p>
                <Link to="/resend-verification" label="verification.resend" />
            </p>
            <p className="divider my-7! flex items-center gap-4 text-[.8125rem] text-slate-600 before:h-px before:flex-1 before:bg-border before:content-[''] after:h-px after:flex-1 after:bg-border after:content-['']">
                {t("login.alternative")}
            </p>
            <Providers />
            <p>
                {t("login.noAccount")}{" "}
                <Link to="/register" label="register.title" />
            </p>
        </>
    );
}
