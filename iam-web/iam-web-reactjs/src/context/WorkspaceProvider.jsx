import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { WorkspaceContext } from "@/context/workspace-context";
import {
    useWorkspaceRoute,
    useWorkspaceQuery,
} from "@/hooks/use-workspace-route";
import { useWorkspaceConfirmation } from "@/hooks/use-workspace-confirmation";
import { createWorkspaceApi } from "@/lib/workspace-api";
import { formatAccountDate } from "@/lib/format-account-date";
import { WorkspaceDialog } from "@/components/workspace/WorkspaceDialog";
export function WorkspaceProvider({ bootstrap, children }) {
    const { language, data, contextPath, csrfToken } = bootstrap;
    const state = useWorkspaceRoute(),
        setQuery = useWorkspaceQuery();
    const [notice, setNotice] = useState("");
    const confirmation = useWorkspaceConfirmation();
    const { t } = useTranslation("workspace", { lng: language });
    const panel = state.panel ?? "overview";
    const api = useMemo(
        () => createWorkspaceApi({ contextPath, csrfToken }),
        [contextPath, csrfToken],
    );
    useEffect(() => {
        document.title = `${t(panel)} · ISTAD`;
    }, [panel, t]);
    useEffect(() => {
        if (!notice) return;
        const timer = setTimeout(() => setNotice(""), 6000);
        return () => clearTimeout(timer);
    }, [notice]);
    const context = {
        t,
        api,
        data,
        language,
        state,
        setQuery,
        url: (path) => contextPath + path,
        can: (permission) => data.permissions?.includes(permission),
        confirm: confirmation.confirm,
        notify: setNotice,
        formatDate: (value) =>
            formatAccountDate(value, {
                language,
                zone: data.claims?.zoneinfo || "Asia/Phnom_Penh",
                unknown: t("unknown"),
            }),
        setAppDirty: bootstrap.setDirty,
    };
    return (
        <WorkspaceContext.Provider value={context}>
            <section className="workspace-content min-w-0" key={panel}>
                <div
                    role="status"
                    className="workspace-feedback my-2 min-h-[1.7rem] text-[.8rem] text-primary"
                >
                    {notice}
                </div>
                {children}
            </section>
            <WorkspaceDialog {...confirmation} />
        </WorkspaceContext.Provider>
    );
}
