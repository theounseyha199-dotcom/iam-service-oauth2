import { Notice } from "@/components/Notice";
import { EmailForm } from "@/components/account/EmailForm";
import { Link } from "@/components/account/AccountLink";
import { BackToLogin } from "@/components/account/BackToLogin";
import { usePage } from "@/context/page-context";

export function RegistrationPendingPage() {
    const { data: d, t } = usePage();
    return (
        <>
            <p>{t("verification.instructions")}</p>
            <Notice success message={d.sent && t("verification.sent")} />
            <EmailForm
                action="/resend-verification"
                label="verification.resend"
            />
            <p>
                <Link to="/verify-email" label="verification.enterCode" />
            </p>
            <BackToLogin />
        </>
    );
}
