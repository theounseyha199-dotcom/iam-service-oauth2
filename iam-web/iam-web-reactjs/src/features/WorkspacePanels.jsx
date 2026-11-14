import { useWorkspace } from "@/context/workspace-context";
import {
    dashboardNavigation,
    managementResources,
} from "@/lib/dashboard-navigation";
import { ErrorNotice } from "@/components/workspace/ErrorNotice";
import { Overview } from "@/features/account/Overview";
import { Sessions } from "@/features/account/Sessions";
import { Activity } from "@/features/account/Activity";
import { Deactivate } from "@/features/account/Deactivate";
import { RecordList } from "@/features/management/RecordList";
import { Policy } from "@/features/management/Policy";
import { DeactivationRequests } from "@/features/management/DeactivationRequests";
export function WorkspacePanels() {
    const { data, state } = useWorkspace();
    const panel = state.panel ?? "overview";
    const { nav, admin } = dashboardNavigation(data.permissions);
    if (![...nav, ...admin].some(([key]) => key === panel))
        return <ErrorNotice error={{ status: 403 }} />;
    if (managementResources[panel]) return <RecordList kind={panel} />;
    switch (panel) {
        case "sessions":
            return <Sessions />;
        case "history":
            return <Activity history />;
        case "activity":
            return <Activity />;
        case "audit":
            return <Activity audit />;
        case "policy":
            return <Policy />;
        case "reviews":
            return <DeactivationRequests />;
        case "deactivation":
            return <Deactivate />;
        default:
            return <Overview />;
    }
}
