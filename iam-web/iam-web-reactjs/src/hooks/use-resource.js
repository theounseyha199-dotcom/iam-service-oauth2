import { useWorkspace } from "@/context/workspace-context";
import { useState, useEffect } from "react";

export function useResource(path, refreshAccess = false) {
    const { api } = useWorkspace();
    const [result, setResult] = useState({
        loading: true,
        data: null,
        error: null,
    });
    const [revision, setRevision] = useState(0);
    useEffect(() => {
        const controller = new AbortController();
        async function load() {
            setResult({
                loading: true,
                data: null,
                error: null,
            });
            try {
                const data = await api(path, {
                    signal: controller.signal,
                });
                if (!controller.signal.aborted)
                    setResult({
                        loading: false,
                        data,
                        error: null,
                    });
            } catch (error) {
                if (!controller.signal.aborted)
                    setResult({
                        loading: false,
                        data: null,
                        error,
                    });
            }
        }
        load();
        return () => controller.abort();
    }, [api, path, revision]);
    useEffect(() => {
        if (!refreshAccess) return;
        const reload = () => setRevision((v) => v + 1);
        window.addEventListener("iam-access-updated", reload);
        return () => window.removeEventListener("iam-access-updated", reload);
    }, [refreshAccess]);
    return {
        ...result,
        reload: () => setRevision((v) => v + 1),
    };
}
