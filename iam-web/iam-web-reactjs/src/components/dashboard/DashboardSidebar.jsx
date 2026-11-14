import { useState } from "react";
import { ShieldCheck, LogOut } from "lucide-react";
import { AppLink } from "@/components/AppLink";
import { ProfileAvatar } from "@/components/ProfileAvatar";
import { Button } from "@/components/ui/button";
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarHeader,
} from "@/components/ui/sidebar";
import { NavigationGroup } from "./NavigationGroup";
export function DashboardSidebar({ bootstrap, t, nav, admin, panel }) {
    const { data, contextPath, csrfName, csrfToken } = bootstrap;
    const url = (path) => contextPath + path;
    const [signingOut, setSigningOut] = useState(false);
    return (
        <Sidebar
            collapsible="icon"
            className="dashboard-sidebar [&_[data-slot=sidebar-inner]]:border-e [&_[data-slot=sidebar-inner]]:border-border [&_[data-slot=sidebar-content]]:gap-1"
            aria-label={t("navigation")}
            data-close-label={t("closeNavigation")}
        >
            <SidebarHeader>
                <AppLink
                    href={url("/workspace")}
                    className="dashboard-brand flex min-h-9 items-center gap-[.65rem] text-foreground"
                >
                    <span className="dashboard-monogram grid h-9 w-[34px] shrink-0 place-items-center rounded-[9px] bg-primary text-primary-foreground">
                        <ShieldCheck aria-hidden="true" size={21} />
                    </span>
                    <span className="dashboard-brand-copy grid gap-px whitespace-nowrap group-data-[collapsible=icon]:hidden [&_strong]:text-[15px] [&_strong]:font-extrabold [&_strong]:tracking-[.08em] [&>span]:text-[10px] [&>span]:text-slate-500">
                        <strong>ISTAD</strong>
                        <span>{t("identityAccess")}</span>
                    </span>
                </AppLink>
            </SidebarHeader>
            <SidebarContent>
                <NavigationGroup
                    label={t("account")}
                    items={nav}
                    panel={panel}
                    t={t}
                    url={url}
                />
                {admin.length > 0 && (
                    <NavigationGroup
                        label={t("management")}
                        items={admin}
                        panel={panel}
                        t={t}
                        url={url}
                    />
                )}
            </SidebarContent>
            <SidebarFooter>
                <div className="dashboard-user mb-[.65rem] flex min-w-0 items-center gap-[.65rem]">
                    <ProfileAvatar
                        src={data.claims?.pictureUrl}
                        name={[data.claims?.givenName, data.claims?.familyName]
                            .filter(Boolean)
                            .join(" ")}
                        className="size-8 text-xs"
                    />
                    <span className="dashboard-user-copy grid min-w-0 gap-0.5 group-data-[collapsible=icon]:hidden [&_strong]:text-[.8rem] [&_strong]:text-foreground [&>span]:text-[.66rem] [&>span]:text-slate-500 [&>span]:wrap-anywhere">
                        <strong>
                            {data.claims?.givenName} {data.claims?.familyName}
                        </strong>
                        <span>{data.claims?.email}</span>
                    </span>
                </div>
                <form
                    action={url("/logout")}
                    method="post"
                    noValidate
                    onSubmit={() => setSigningOut(true)}
                >
                    <input type="hidden" name={csrfName} value={csrfToken} />
                    <Button
                        type="submit"
                        variant="ghost"
                        className="dashboard-logout w-full justify-start text-[.78rem] group-data-[collapsible=icon]:w-8 group-data-[collapsible=icon]:overflow-hidden group-data-[collapsible=icon]:p-2"
                        disabled={signingOut}
                    >
                        <LogOut aria-hidden="true" />
                        {t("logout")}
                    </Button>
                </form>
            </SidebarFooter>
        </Sidebar>
    );
}
