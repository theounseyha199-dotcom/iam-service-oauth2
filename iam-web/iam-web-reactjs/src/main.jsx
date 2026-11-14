import { I18nextProvider } from "react-i18next";
import { i18n } from "@/i18n/instance";
import { prepareBootstrap } from "@/i18n/prepare-bootstrap";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router/dom";
import { createAppRouter } from "@/router";
import { applyDocumentStyles } from "@/lib/document-styles";
import "./tailwind.css";
applyDocumentStyles();
const element = document.getElementById("iam-page");
const bootstrap = element
    ? prepareBootstrap(JSON.parse(element.textContent))
    : null;
createRoot(document.getElementById("root")).render(
    element ? (
        <StrictMode>
            <I18nextProvider i18n={i18n}>
                <RouterProvider router={createAppRouter(bootstrap)} />
            </I18nextProvider>
        </StrictMode>
    ) : (
        <p>
            Open the application through <a href="/login">ISTAD sign in</a>.
        </p>
    ),
);
