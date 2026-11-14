import { Notice } from "@/components/Notice";
import { Link } from "@/components/account/AccountLink";
import { Form } from "@/components/account/AccountForm";
import { PasswordFields } from "@/components/account/PasswordFields";
import { Submit } from "@/components/account/Submit";
import { BackToLogin } from "@/components/account/BackToLogin";
import { usePage } from "@/context/page-context";

export function ResetPasswordPage() {
    const { data: d, t } = usePage();
    return (
        <>
            {d.done ? (
                <Notice success message={t("recovery.done")} />
            ) : d.errorCode === "invalid_reset_token" ? (
                <p>
                    <Link to="/forgot-password" label="recovery.sendAgain" />
                </p>
            ) : (
                <Form noValidate action="/reset-password">
                    <PasswordFields reset />
                    <Submit label="recovery.reset" />
                </Form>
            )}
            <BackToLogin />
        </>
    );
}
