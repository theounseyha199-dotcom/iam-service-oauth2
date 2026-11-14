import { Form } from "@/components/account/AccountForm";
import { Hidden } from "@/components/account/Hidden";
import { Field } from "@/components/account/Field";
import { PasswordFields } from "@/components/account/PasswordFields";
import { Submit } from "@/components/account/Submit";
import { Link } from "@/components/account/AccountLink";
import { usePage } from "@/context/page-context";

export function RegisterPage() {
    const { t, language } = usePage();
    return (
        <>
            <p>{t("register.intro")}</p>
            <Form noValidate action="/register">
                <Hidden name="locale" value={language} />
                {["username", "email", "givenName", "familyName"].map(
                    (name) => (
                        <Field
                            key={name}
                            name={name}
                            type={name === "email" ? "email" : "text"}
                            required
                            maxLength={name === "email" ? 255 : 100}
                        />
                    ),
                )}
                <PasswordFields />
                <Submit label="register.submit" />
            </Form>
            <p>
                {t("register.haveAccount")}{" "}
                <Link to="/login" label="login.title" />
            </p>
        </>
    );
}
