import { ShieldCheck } from "lucide-react";
import { Badge } from "@/components/ui/badge";
export function PageHeading({ t, admin, panel, heading }) {
    const management = admin.some(([key]) => key === panel);
    const compact = panel === "apps";
    return (
        <div className="dashboard-page-heading mb-2 flex items-center justify-between gap-4 max-md:items-start [&_h1]:m-0 [&_h1]:text-[clamp(1.75rem,3vw,1.875rem)] [&_h1]:leading-[1.35] [&_h1]:font-[650] [&_h1]:tracking-[-.035em] [&_h1]:text-foreground">
            <div>
                {!compact && (
                    <p className="dashboard-eyebrow m-0 mb-[.4rem] text-[.8125rem] font-bold tracking-[.12em] text-primary uppercase">
                        {t(management ? "management" : "account")}
                    </p>
                )}
                <h1>{heading ?? t(panel)}</h1>
                {!compact && (
                    <p className="dashboard-description mt-[.45rem] text-sm text-muted-foreground">
                        {t("intro")}
                    </p>
                )}
            </div>
            {management && (
                <Badge
                    variant="outline"
                    className="dashboard-access-badge inline-flex items-center gap-[.4rem] bg-card text-[.8125rem] text-primary max-md:hidden"
                >
                    <ShieldCheck aria-hidden="true" size={14} />
                    {t("adminAccess")}
                </Badge>
            )}
        </div>
    );
}
