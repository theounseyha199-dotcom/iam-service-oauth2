import { createElement } from "react";
import { createBrowserRouter } from "react-router";
import { App } from "@/App";
import { pageRoutes } from "@/lib/route-paths";
import { loadPage, shouldReloadPage } from "@/lib/page-loader";
import { RouteContent } from "@/components/RouteContent";
import { RouteLoading } from "@/components/RouteLoading";
import { ErrorPage } from "@/pages/ErrorPage";
import { RouteError } from "@/components/RouteError";

const pages = {
    WorkspacePage: () =>
        import("@/pages/WorkspacePage").then((module) => module.WorkspacePage),
    LoginPage: () =>
        import("@/pages/LoginPage").then((module) => module.LoginPage),
    RegisterPage: () =>
        import("@/pages/RegisterPage").then((module) => module.RegisterPage),
    ProfileEditPage: () =>
        import("@/pages/ProfileEditPage").then(
            (module) => module.ProfileEditPage,
        ),
    ChangePasswordPage: () =>
        import("@/pages/ChangePasswordPage").then(
            (module) => module.ChangePasswordPage,
        ),
    LinkedAccountsPage: () =>
        import("@/pages/LinkedAccountsPage").then(
            (module) => module.LinkedAccountsPage,
        ),
    UnlinkAccountPage: () =>
        import("@/pages/UnlinkAccountPage").then(
            (module) => module.UnlinkAccountPage,
        ),
    ConnectedAppsPage: () =>
        import("@/pages/ConnectedAppsPage").then(
            (module) => module.ConnectedAppsPage,
        ),
    RevokeAppPage: () =>
        import("@/pages/RevokeAppPage").then((module) => module.RevokeAppPage),
    ConsentPage: () =>
        import("@/pages/ConsentPage").then((module) => module.ConsentPage),
    LogoutPage: () =>
        import("@/pages/LogoutPage").then((module) => module.LogoutPage),
    FederationCompletePage: () =>
        import("@/pages/FederationCompletePage").then(
            (module) => module.FederationCompletePage,
        ),
    ForgotPasswordPage: () =>
        import("@/pages/ForgotPasswordPage").then(
            (module) => module.ForgotPasswordPage,
        ),
    ResetPasswordPage: () =>
        import("@/pages/ResetPasswordPage").then(
            (module) => module.ResetPasswordPage,
        ),
    RegistrationPendingPage: () =>
        import("@/pages/RegistrationPendingPage").then(
            (module) => module.RegistrationPendingPage,
        ),
    VerifyEmailPage: () =>
        import("@/pages/VerifyEmailPage").then(
            (module) => module.VerifyEmailPage,
        ),
};

export function createAppRouter(bootstrap) {
    return createBrowserRouter(
        [
            {
                id: "iam",
                path: bootstrap.contextPath || "/",
                Component: App,
                loader: loadPage,
                shouldRevalidate: shouldReloadPage,
                ErrorBoundary: RouteError,
                HydrateFallback: RouteLoading,
                children: [
                    ...pageRoutes.map(([path, name]) => ({
                        ...(path ? { path } : { index: true }),
                        lazy: async () => ({
                            element: createElement(RouteContent, {
                                Page: await pages[name](),
                            }),
                        }),
                    })),
                    {
                        path: "*",
                        Component: ErrorPage,
                    },
                ],
            },
        ],
        { hydrationData: { loaderData: { iam: bootstrap } } },
    );
}
