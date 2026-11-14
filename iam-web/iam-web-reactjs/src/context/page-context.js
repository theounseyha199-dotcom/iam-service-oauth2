import { createContext } from "react";
export const PageContext = createContext(null);
import { useContext } from "react";

export function usePage() {
    return useContext(PageContext);
}
