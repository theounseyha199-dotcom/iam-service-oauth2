import { Form } from "@/components/account/AccountForm";
import { Hidden } from "@/components/account/Hidden";
import { Field } from "@/components/account/Field";
import { Submit } from "@/components/account/Submit";
import { Button } from "@/components/ui/button";
import { usePage } from "@/context/page-context";

export function FederationCompletePage() {
    const { data: d, t } = usePage();
    return (
        <>
            <p>{t("completion.intro")}</p>
            <Form noValidate action="/federation/complete">
                <Hidden name="flow" value={d.flow} />
                {[
                    ["email", d.needEmail],
                    ["givenName", d.needGivenName],
                    ["familyName", d.needFamilyName],
                ]
                    .filter(([, needed]) => needed)
                    .map(([name]) => (
                        <Field
                            key={name}
                            name={name}
                            value={d[name]}
                            type={name === "email" ? "email" : "text"}
                            required
                            maxLength={name === "email" ? 255 : 100}
                        />
                    ))}
                {d.needEmail && (
                    <p className="hint text-sm text-muted-foreground [.page_&]:m-0! [.page_&]:rounded [.page_&]:border-s-2 [.page_&]:border-brand-200 [.page_&]:bg-slate-50 [.page_&]:px-4 [.page_&]:py-3 [.dashboard-root_&]:text-xs">
                        {t("completion.emailHint")}
                    </p>
                )}
                <Submit label="completion.submit" />
            </Form>
            <Form noValidate action="/federation/cancel">
                <Button
                    variant="outline"
                    size="account"
                    className="secondary border-primary bg-card text-brand-800 hover:bg-accent active:bg-brand-100"
                    type="submit"
                >
                    {t("completion.cancel")}
                </Button>
            </Form>
        </>
    );
}
