import { usePage } from "@/context/page-context";
import { AppLink } from "@/components/AppLink";
import { buttonVariants } from "@/components/ui/button";

export function Link({ to, label, children, button = false }) {
    const { url, t } = usePage();
    return (
        <AppLink
            className={
                button
                    ? buttonVariants({
                          variant: "outline",
                          size: "account",
                          className:
                              "button inline-flex items-center justify-center gap-2 rounded-lg border border-primary bg-card px-[1.125rem] py-3 text-center font-semibold text-brand-800 no-underline hover:bg-accent active:bg-brand-100 [.dashboard-root_&]:rounded-md [.dashboard-root_&]:border-border [.dashboard-root_&]:px-[.8rem] [.dashboard-root_&]:py-[.45rem] [.dashboard-root_&]:text-xs secondary border-primary bg-card text-brand-800 hover:bg-accent active:bg-brand-100",
                      })
                    : "text-brand-800 underline underline-offset-[.2em] hover:text-brand-600 active:text-brand-900"
            }
            href={url(to)}
        >
            {label ? t(label) : children}
        </AppLink>
    );
}
