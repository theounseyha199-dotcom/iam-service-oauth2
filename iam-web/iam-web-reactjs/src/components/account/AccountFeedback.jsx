import { Notice } from "@/components/Notice";
import { usePage } from "@/context/page-context";
export function AccountFeedback({ children }) {
    const { data, page, messages, t } = usePage();
    const errorCode =
        data.errorCode || (page === "federation-complete" ? data.error : "");
    return (
        <>
            <Notice
                message={
                    errorCode &&
                    (messages[`error.${errorCode}`] ?? t("error.generic"))
                }
            />
            <Notice success message={data.saved && t("common.saved")} />
            {children}
        </>
    );
}
