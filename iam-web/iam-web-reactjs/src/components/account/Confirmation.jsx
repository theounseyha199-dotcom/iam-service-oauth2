import { usePage } from "@/context/page-context";
import { Link } from "@/components/account/AccountLink";
import { Form } from "@/components/account/AccountForm";
import { Submit } from "@/components/account/Submit";

export function Confirmation({ object, warning, action, cancel, label }) {
    const { t } = usePage();
    return (
        <>
            <p>{t(warning, object)}</p>
            <div className="actions mt-5 flex flex-wrap items-end justify-end gap-3 [&_form]:m-0! [.dashboard-root_&]:items-center [.dashboard-root_&]:gap-[.65rem]">
                <Link to={cancel} button label="common.cancel" />
                <Form noValidate action={action}>
                    <Submit label={label} danger />
                </Form>
            </div>
        </>
    );
}
