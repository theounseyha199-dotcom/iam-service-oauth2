import { Form } from "@/components/account/AccountForm";
import { Submit } from "@/components/account/Submit";
import { Link } from "@/components/account/AccountLink";
import { usePage } from "@/context/page-context";

export function LogoutPage() {
    const { t } = usePage();
    return (
        <>
            <p>{t("logout.confirm")}</p>
            <Form noValidate action="/logout">
                <Submit label="common.logout" />
            </Form>
            <p>
                <Link to="/" label="common.cancel" />
            </p>
        </>
    );
}
