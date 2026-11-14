import { WorkspaceProvider } from "@/context/WorkspaceProvider";
import { WorkspacePanels } from "@/features/WorkspacePanels";
export function Workspace({ bootstrap }) {
    return (
        <WorkspaceProvider bootstrap={bootstrap}>
            <WorkspacePanels />
        </WorkspaceProvider>
    );
}
