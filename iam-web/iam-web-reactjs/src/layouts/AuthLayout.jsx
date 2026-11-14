import { useNavigation } from "react-router";
import { AppLink } from "@/components/AppLink";
import { Card } from "@/components/ui/card";
import { usePage } from "@/context/page-context";
export function AuthLayout({ title, children }) {
    const pending = useNavigation().state === "loading";
    const { page, language, languagePath, t, url } = usePage();
    const languageLink = (lang) =>
        `${languagePath}${languagePath.includes("?") ? "&" : "?"}lang=${lang}`;
    return (
        <>
            <header className="site-header mx-auto flex max-w-[1160px] items-center justify-between gap-4 p-6 max-[700px]:px-4 max-[480px]:gap-2 max-[480px]:p-4 [&_nav]:flex [&_nav]:gap-1 [&_nav]:rounded-lg [&_nav]:border [&_nav]:border-border [&_nav]:bg-card [&_nav]:p-1 [&_nav_a]:rounded [&_nav_a]:px-3 [&_nav_a]:py-[.375rem] [&_nav_a]:text-[.8125rem] [&_nav_a]:no-underline [&_nav_a:hover]:bg-slate-100 [&_nav_a[aria-current=true]]:bg-accent [&_nav_a[aria-current=true]]:text-brand-800 max-[480px]:[&_nav_a]:px-2">
                <AppLink
                    className="brand m-0 text-xl font-extrabold tracking-[.12em] text-brand-800 no-underline max-[480px]:text-base max-[480px]:tracking-[.06em]"
                    href={url("/login")}
                >
                    {t("common.brand")}
                </AppLink>
                <nav aria-label={t("common.language")}>
                    {["en", "km"].map((lang) => (
                        <AppLink
                            key={lang}
                            href={languageLink(lang)}
                            lang={lang}
                            aria-current={language === lang ? "true" : "false"}
                        >
                            {t(
                                lang === "en"
                                    ? "common.english"
                                    : "common.khmer",
                            )}
                        </AppLink>
                    ))}
                </nav>
            </header>
            <main
                aria-busy={pending}
                inert={pending}
                className="page aria-busy:invisible mx-auto max-w-[1120px] px-4 pt-8 pb-16 [&_h1]:mb-[.625rem] [&_h1]:text-[clamp(1.75rem,4vw,2.25rem)] [&_h1]:leading-[1.3] [&_h1]:font-[750] [&_h1]:tracking-[-.035em] [&_h1]:text-foreground [&_h2]:mb-3 [&_h2]:text-xl [&_h2]:font-bold [&_h3]:mt-4 [&_h3]:mb-2 [&_h3]:text-base [&_p]:my-3 [&_form]:mt-6 [&_form]:grid [&_form]:gap-[1.125rem] [&_fieldset]:grid [&_fieldset]:min-w-0 [&_fieldset]:gap-[1.125rem] [&_fieldset]:border-0 [&_fieldset]:p-0 [&_fieldset:disabled]:opacity-70 [&_select]:block [&_select]:w-full [&_select]:min-h-[3.125rem] [&_select]:rounded-lg [&_select]:border [&_select]:border-input [&_select]:bg-card [&_select]:px-3.5 [&_select]:py-3 [&_select]:text-foreground [&_select:hover]:border-slate-400 [&_select:focus]:border-primary [&_select:focus]:outline-3 [&_select:focus]:outline-brand-100 [&_select:focus]:outline-offset-1 auth-page max-w-[600px] pt-[clamp(1.5rem,6vh,4rem)] has-[>.login-card]:max-w-[492px] has-[>.login-card]:pt-[clamp(1rem,4vh,2.5rem)] max-[480px]:pt-4 [&>section]:border-t-[3px] [&>section]:border-t-primary [&>section]:p-[clamp(1.5rem,5vw,3rem)] [&>section>p:last-child]:mt-7 [&>section>p:last-child]:mb-0 [&>section>p:last-child]:border-t [&>section>p:last-child]:border-border [&>section>p:last-child]:pt-6 [&>section>p:last-child]:text-center [&>section>p:last-child]:text-sm"
            >
                <Card
                    as="section"
                    className={`card block min-w-0 rounded-2xl border border-border bg-card p-[clamp(1.25rem,4vw,2rem)] shadow-none [.dashboard-root_&]:rounded-[10px] [.dashboard-root_&]:p-[1.4rem] ${["login", "forgot-password", "reset-password", "registration-pending", "verify-email"].includes(page) ? "login-card p-[clamp(1.25rem,4vw,1.5rem)]! [&_h1]:text-[1.875rem]! [&_form]:mt-4! [&_form]:gap-[.375rem]! [&_fieldset]:gap-[.375rem]! [&>.eyebrow]:mb-3 [&>.eyebrow]:flex [&>.eyebrow]:items-center [&>.eyebrow]:gap-[.625rem] [&>.intro]:mb-4! [&>.intro]:text-sm [&>.divider]:my-4! [&>.divider]:gap-[.625rem] [&_.provider-grid]:grid-cols-2 [&_.provider-grid]:gap-[.625rem] max-[350px]:[&_.provider-grid]:grid-cols-1 [&_.provider-grid_a]:min-h-[2.875rem] [&_.provider-grid_a]:px-2 [&_.provider-grid_a]:py-[.625rem] [&>p:last-child]:mt-4! [&>p:last-child]:pt-4! [&_button[type=submit]]:min-w-0 [&_button:disabled]:bg-brand-800 [&_button:disabled]:opacity-100" : ""}`}
                >
                    {page !== "login" && <h1>{title}</h1>}
                    {children}
                </Card>
            </main>
        </>
    );
}
