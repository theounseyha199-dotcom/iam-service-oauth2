import { useCallback } from "react";
import { useLocation, useSearchParams } from "react-router";

export function useWorkspaceRoute() {
    return Object.fromEntries(new URLSearchParams(useLocation().search));
}
export function useWorkspaceQuery() {
    const [, setSearchParams] = useSearchParams();
    return useCallback(
        (patch) =>
            setSearchParams((previous) => {
                const next = new URLSearchParams(previous);
                Object.entries(patch).forEach(([key, value]) => {
                    if (value === "" || value == null) next.delete(key);
                    else next.set(key, String(value));
                });
                return next;
            }),
        [setSearchParams],
    );
}
