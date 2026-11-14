import { ChevronRight } from "lucide-react";
import { AppLink } from "@/components/AppLink";
import { buttonVariants } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { SidebarTrigger } from "@/components/ui/sidebar";
export function DashboardHeader({ bootstrap, t, panel }) {
    const { languagePath, language } = bootstrap;
    const languageLink = (lang) => {
        const target = new URL(
            languagePath || window.location.pathname,
            window.location.origin,
        );
        // Client-side navigation is authoritative until the next server render.
        target.search = window.location.search;
        target.searchParams.set("lang", lang);
        return target.pathname + target.search;
    };
    return (
        <header className="dashboard-header flex min-h-[65px] items-center justify-between gap-4 border-b border-border bg-card px-8 py-3 max-md:gap-2 max-md:px-4">
            <div className="dashboard-breadcrumb flex min-w-0 items-center gap-3 text-xs text-slate-500 max-md:gap-2 max-md:[&>svg]:hidden">
                <SidebarTrigger
                    aria-label={t("toggleNavigation")}
                    title={t("toggleNavigation")}
                />
                <Separator orientation="vertical" className="h-5" />
                <span className="dashboard-parent max-md:hidden">
                    {t("workspace")}
                </span>
                <ChevronRight size={14} aria-hidden="true" />
                <span className="dashboard-current font-medium text-foreground">
                    {t(panel)}
                </span>
            </div>
            <nav
                aria-label={t("language")}
                className="dashboard-language flex shrink-0 gap-[.2rem]"
            >
                {["en", "km"].map((lang) => (
                    <AppLink
                        key={lang}
                        data-slot="button"
                        className={buttonVariants({
                            size: "sm",
                            variant: language === lang ? "secondary" : "ghost",
                        })}
                        href={languageLink(lang)}
                        lang={lang}
                        aria-current={language === lang ? "true" : undefined}
                    >
                        {lang === "en" ? "English" : "ខ្មែរ"}
                    </AppLink>
                ))}
            </nav>
        </header>
    );
}
