import { Workspace } from "@/Workspace";
import { usePage } from "@/context/page-context";

export function WorkspacePage() {
    const { ...bootstrap } = usePage();
    return <Workspace bootstrap={bootstrap} />;
}
