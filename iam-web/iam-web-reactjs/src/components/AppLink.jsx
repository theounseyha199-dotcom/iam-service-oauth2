import { Link } from "react-router";
import { isAppPath } from "@/lib/route-paths";

/** Protocol endpoints and external destinations remain native browser navigations. */
export function AppLink({ href, to, children, ...props }) {
    const target = to ?? href;
    const url = new URL(target, window.location.href);
    if (url.origin === window.location.origin && isAppPath(url.pathname)) {
        return (
            <Link
                {...props}
                data-router-link="true"
                to={url.pathname + url.search + url.hash}
            >
                {children}
            </Link>
        );
    }
    return (
        <a {...props} href={target}>
            {children}
        </a>
    );
}
