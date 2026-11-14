import { createContext } from "react";
export const WorkspaceContext = createContext(null);
import { useContext } from "react";

export function useWorkspace() {
    return useContext(WorkspaceContext);
}
