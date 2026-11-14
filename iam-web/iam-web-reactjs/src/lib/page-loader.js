import { prepareBootstrap } from "../i18n/prepare-bootstrap.js";
import { redirect } from "react-router";

/** Server controllers remain authoritative for identity, permission and CSRF projections. */
export async function loadPage({ request }) {
    const response = await fetch(request.url, {
        credentials: "same-origin",
        cache: "no-store",
        signal: request.signal,
        headers: { Accept: "text/html" },
    });
    const requested = new URL(request.url);
    const destination = new URL(response.url);
    if (destination.origin !== requested.origin)
        throw new Response(null, { status: 502 });
    if (response.redirected && destination.href !== requested.href) {
        return redirect(
            destination.pathname + destination.search + destination.hash,
        );
    }
    const document = new DOMParser().parseFromString(
        await response.text(),
        "text/html",
    );
    const element = document.getElementById("iam-page");
    if (!element)
        throw new Response(null, {
            status: response.ok ? 502 : response.status,
        });
    const bootstrap = JSON.parse(element.textContent);
    if (!bootstrap.page || !bootstrap.messages || !bootstrap.data)
        throw new Response(null, { status: 502 });
    prepareBootstrap(bootstrap);
    return { ...bootstrap, clientNavigationKey: crypto.randomUUID() };
}

export function shouldReloadPage({ currentUrl, nextUrl }) {
    const workspace = (path) => /\/(workspace)?$/.test(path);
    if (
        currentUrl.pathname === nextUrl.pathname &&
        workspace(nextUrl.pathname) &&
        currentUrl.searchParams.get("lang") === nextUrl.searchParams.get("lang")
    )
        return false;
    return true;
}
