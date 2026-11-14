import { useAccountTranslation } from "@/hooks/use-account-translation";
import { useEffect, useState } from "react";
import { Outlet, useLoaderData, useLocation } from "react-router";
import { PageContext } from "@/context/page-context";
import { titles, accountPages } from "@/context/account-config";
import { AccountLayout } from "@/layouts/AccountLayout";
import { AuthLayout } from "@/layouts/AuthLayout";
import { RoutePendingStatus } from "@/components/RoutePendingStatus";
import { AccountFeedback } from "@/components/account/AccountFeedback";
import { UnsavedChangesGuard } from "@/components/UnsavedChangesGuard";

export function App() {
    const bootstrap = useLoaderData();
    const location = useLocation();
    const [dirty, setDirty] = useState(false);
    const { page, contextPath, language } = bootstrap;
    const t = useAccountTranslation(language);
    const url = (path) => contextPath + path;
    const title = t(titles[page] ?? "error.pageTitle");
    useEffect(() => {
        if (page !== "index") document.title = `${title} · ISTAD`;
        document.documentElement.lang = language;
        (
            document.querySelector('[aria-invalid="true"]') ??
            document.querySelector('[role="alert"][tabindex]')
        )?.focus();
    }, [title, language, page, location.pathname]);
    const Layout = accountPages.has(page) ? AccountLayout : AuthLayout;
    return (
        <PageContext.Provider value={{ ...bootstrap, t, url, setDirty }}>
            <UnsavedChangesGuard dirty={dirty} setDirty={setDirty}>
                <Layout bootstrap={bootstrap} title={title}>
                    <AccountFeedback>
                        <Outlet
                            key={
                                bootstrap.clientNavigationKey ??
                                page + location.pathname + language
                            }
                        />
                    </AccountFeedback>
                </Layout>
                <RoutePendingStatus />
            </UnsavedChangesGuard>
        </PageContext.Provider>
    );
}
