import { Form } from "@/components/account/AccountForm";
import { Hidden } from "@/components/account/Hidden";
import { Submit } from "@/components/account/Submit";
import { Button } from "@/components/ui/button";
import { usePage } from "@/context/page-context";

export function ConsentPage() {
    const { data: d, t } = usePage();
    return (
        <>
            <p>{t("consent.intro", d.client?.name)}</p>
            <p>{t("consent.hint")}</p>
            <Form noValidate action="/oauth2/authorize">
                <Hidden name="client_id" value={d.clientId} />
                <Hidden name="state" value={d.state} />
                {[...(d.client?.scopes ?? [])].sort().map((scope) =>
                    scope === "openid" ? (
                        <p key={scope}>{t(`scope.${scope}`)}</p>
                    ) : (
                        <label
                            className="check flex cursor-pointer items-center gap-3 py-2 [&_input]:m-0 [&_input]:size-5 [&_input]:min-h-0 [&_input]:shrink-0 [&_input]:appearance-auto [&_input]:accent-primary"
                            key={scope}
                        >
                            <input
                                type="checkbox"
                                name="scope"
                                value={scope}
                                defaultChecked
                            />
                            {t(`scope.${scope}`)}
                        </label>
                    ),
                )}
                <Submit label="consent.allow" />
            </Form>
            <Form noValidate action="/oauth2/authorize">
                <Hidden name="client_id" value={d.clientId} />
                <Hidden name="state" value={d.state} />
                <Button
                    variant="outline"
                    size="account"
                    className="secondary border-primary bg-card text-brand-800 hover:bg-accent active:bg-brand-100"
                    type="submit"
                >
                    {t("consent.cancel")}
                </Button>
            </Form>
        </>
    );
}
