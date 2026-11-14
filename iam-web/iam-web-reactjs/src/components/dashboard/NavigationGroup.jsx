import {
    Activity,
    AppWindow,
    ArrowRightLeft,
    CircleUserRound,
    ClipboardList,
    History,
    KeyRound,
    LayoutDashboard,
    Link2,
    LockKeyhole,
    Monitor,
    Settings2,
    ShieldCheck,
    ShieldOff,
    UsersRound,
} from "lucide-react";
import { AppLink } from "@/components/AppLink";
import {
    SidebarGroup,
    SidebarGroupContent,
    SidebarGroupLabel,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    useSidebar,
} from "@/components/ui/sidebar";
const icons = {
    overview: LayoutDashboard,
    profile: CircleUserRound,
    security: LockKeyhole,
    linked: Link2,
    apps: AppWindow,
    sessions: Monitor,
    history: History,
    activity: Activity,
    deactivation: ShieldOff,
    users: UsersRound,
    roles: ShieldCheck,
    permissions: KeyRound,
    clients: ArrowRightLeft,
    audit: ClipboardList,
    policy: Settings2,
    reviews: ShieldOff,
};
export function NavigationGroup({ label, items, panel, t, url }) {
    const { setOpenMobile } = useSidebar();
    return (
        <SidebarGroup>
            <SidebarGroupLabel>{label}</SidebarGroupLabel>
            <SidebarGroupContent>
                <nav aria-label={label}>
                    <SidebarMenu>
                        {items.map(([key, path]) => {
                            const Icon = icons[key];
                            return (
                                <SidebarMenuItem key={key}>
                                    <SidebarMenuButton
                                        asChild
                                        isActive={panel === key}
                                        tooltip={t(key)}
                                    >
                                        <AppLink
                                            href={
                                                path
                                                    ? url(path)
                                                    : url(
                                                          `/workspace?panel=${key}`,
                                                      )
                                            }
                                            aria-current={
                                                panel === key
                                                    ? "page"
                                                    : undefined
                                            }
                                            onClick={(event) => {
                                                if (
                                                    event.defaultPrevented ||
                                                    event.ctrlKey ||
                                                    event.metaKey ||
                                                    event.shiftKey ||
                                                    event.altKey ||
                                                    event.button !== 0
                                                )
                                                    return;
                                                setOpenMobile(false);
                                            }}
                                        >
                                            <Icon aria-hidden="true" />
                                            <span>{t(key)}</span>
                                        </AppLink>
                                    </SidebarMenuButton>
                                </SidebarMenuItem>
                            );
                        })}
                    </SidebarMenu>
                </nav>
            </SidebarGroupContent>
        </SidebarGroup>
    );
}
