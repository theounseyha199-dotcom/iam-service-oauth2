import { useNavigation } from "react-router";
import { SidebarProvider, SidebarInset } from "@/components/ui/sidebar";
import { DashboardSidebar } from "@/components/dashboard/DashboardSidebar";
import { DashboardHeader } from "@/components/dashboard/DashboardHeader";
import { PageHeading } from "@/components/dashboard/PageHeading";
export function DashboardShell({
    bootstrap,
    t,
    nav,
    admin,
    panel,
    children,
    heading,
}) {
    const pending = useNavigation().state === "loading";
    return (
        <SidebarProvider
            className="dashboard-layout items-stretch"
            defaultOpen={!document.cookie.includes("sidebar_state=false")}
        >
            <DashboardSidebar
                bootstrap={bootstrap}
                t={t}
                nav={nav}
                admin={admin}
                panel={panel}
            />
            <SidebarInset className="dashboard-inset min-w-0 bg-background">
                <DashboardHeader bootstrap={bootstrap} t={t} panel={panel} />
                <div
                    aria-busy={pending}
                    inert={pending}
                    className="dashboard-content aria-busy:invisible mx-auto w-full max-w-[1550px] px-[clamp(1rem,3vw,2.5rem)] pt-8 pb-12 max-md:px-4 max-md:pt-5 max-md:pb-8"
                >
                    <PageHeading
                        t={t}
                        admin={admin}
                        panel={panel}
                        heading={heading}
                    />
                    {children}
                </div>
            </SidebarInset>
        </SidebarProvider>
    );
}
