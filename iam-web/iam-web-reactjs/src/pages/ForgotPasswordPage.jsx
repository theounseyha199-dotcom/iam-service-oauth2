import { Notice } from "@/components/Notice";
import { EmailForm } from "@/components/account/EmailForm";
import { BackToLogin } from "@/components/account/BackToLogin";
import { usePage } from "@/context/page-context";

export function ForgotPasswordPage() {
    const { data: d, t } = usePage();
    return (
        <>
            <p>{t("recovery.intro")}</p>
            {d.sent ? (
                <Notice success message={t("recovery.sent")} />
            ) : (
                <EmailForm action="/forgot-password" label="recovery.send" />
            )}
            <BackToLogin />
        </>
    );
}
