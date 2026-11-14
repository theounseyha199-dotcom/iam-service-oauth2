import { BackToLogin } from "@/components/account/BackToLogin";
import { usePage } from "@/context/page-context";

export function ErrorPage() {
    const { data: d, t } = usePage();
    return (
        <>
            <p>{t("error.page", d.status ?? 500)}</p>
            <BackToLogin />
        </>
    );
}
