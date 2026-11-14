import { Form } from "@/components/account/AccountForm";
import { PasswordFields } from "@/components/account/PasswordFields";
import { Submit } from "@/components/account/Submit";
import { Link } from "@/components/account/AccountLink";
import { usePage } from "@/context/page-context";

export function ChangePasswordPage() {
    const { t } = usePage();
    return (
        <>
            <p>{t("profile.passwordHint")}</p>
            <Form noValidate action="/profile/password">
                <PasswordFields current />
                <Submit label="profile.password" />
            </Form>
            <p>
                <Link to="/forgot-password" label="recovery.title" />
            </p>
        </>
    );
}
