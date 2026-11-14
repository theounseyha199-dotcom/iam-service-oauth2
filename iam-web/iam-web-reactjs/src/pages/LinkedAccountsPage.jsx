import { Notice } from "@/components/Notice";
import { ConnectedProvider } from "@/components/account/ConnectedProvider";
import { Providers } from "@/components/account/Providers";
import { usePage } from "@/context/page-context";

export function LinkedAccountsPage() {
    const { data: d, t } = usePage();
    return (
        <>
            <p>{t("accounts.intro")}</p>
            <Notice
                success
                message={
                    (d.linked || d.unlinked) &&
                    t(d.linked ? "accounts.linked" : "accounts.unlinked")
                }
            />
            {d.failed && !d.errorCode && (
                <Notice message={t(`error.${d.errorCode || "generic"}`)} />
            )}
            {d.identities?.length ? (
                <ul className="record-list list-none p-0 [&>li]:flex [&>li]:items-center [&>li]:justify-between [&>li]:gap-4 [&>li]:border-b [&>li]:border-border [&>li]:py-5 [&>li>div]:min-w-0 [&>li>div]:wrap-anywhere [&>li_a]:shrink-0 max-[700px]:[&>li]:flex-col max-[700px]:[&>li]:items-stretch">
                    {d.identities.map((i) => (
                        <ConnectedProvider key={i.uuid} identity={i} />
                    ))}
                </ul>
            ) : (
                <p className="empty">{t("accounts.empty")}</p>
            )}
            <Providers link />
        </>
    );
}
