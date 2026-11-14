import { usePage } from "@/context/page-context";
import { Form } from "@/components/account/AccountForm";
import { Field } from "@/components/account/Field";
import { Submit } from "@/components/account/Submit";

export function EmailForm({ action, label }) {
    const { data } = usePage();
    return (
        <Form noValidate action={action}>
            <Field
                name="email"
                type="email"
                required
                value={data.email}
                error={data.errorCode ? "validation.email" : ""}
            />
            <Submit label={label} />
        </Form>
    );
}
