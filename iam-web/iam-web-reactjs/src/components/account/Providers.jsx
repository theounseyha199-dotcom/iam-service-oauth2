import { usePage } from "@/context/page-context";
import { Form } from "@/components/account/AccountForm";
import { Button, buttonVariants } from "@/components/ui/button";
import { AppLink } from "@/components/AppLink";

export function Providers({ link = false }) {
    const { providers, t, url, data } = usePage();
    const available = providers.filter(
        (provider) =>
            !link ||
            !data.identities?.some(
                (identity) => identity.provider === provider,
            ),
    );
    if (!available.length) return <p>{t("accounts.allConnected")}</p>;
    return (
        <div className="provider-grid grid grid-cols-2 gap-3 max-[700px]:grid-cols-1 [&_form]:m-0! [&_a]:border-input [&_a]:text-sm [&_a]:text-slate-700 [&_a:hover]:border-primary [&_a:hover]:text-brand-800">
            {available.map((provider) =>
                link ? (
                    <Form
                        noValidate
                        key={provider}
                        action={`/profile/accounts/link/${provider}`}
                    >
                        <Button
                            variant="outline"
                            size="account"
                            className="secondary border-primary bg-card text-brand-800 hover:bg-accent active:bg-brand-100"
                            type="submit"
                        >
                            {t("accounts.connect", t(`provider.${provider}`))}
                        </Button>
                    </Form>
                ) : (
                    <AppLink
                        key={provider}
                        className={buttonVariants({
                            variant: "outline",
                            size: "account",
                            className:
                                "button inline-flex items-center justify-center gap-2 rounded-lg border border-primary bg-card px-[1.125rem] py-3 text-center font-semibold text-brand-800 no-underline hover:bg-accent active:bg-brand-100 [.dashboard-root_&]:rounded-md [.dashboard-root_&]:border-border [.dashboard-root_&]:px-[.8rem] [.dashboard-root_&]:py-[.45rem] [.dashboard-root_&]:text-xs secondary border-primary bg-card text-brand-800 hover:bg-accent active:bg-brand-100",
                        })}
                        href={url(`/oauth2/authorization/${provider}`)}
                    >
                        <img
                            className="provider-icon shrink-0"
                            src={url(`/images/${provider}.svg`)}
                            width="20"
                            height="20"
                            alt=""
                        />
                        {t(`provider.${provider}`)}
                    </AppLink>
                ),
            )}
        </div>
    );
}
