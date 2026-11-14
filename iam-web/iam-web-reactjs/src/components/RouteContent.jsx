import { usePage } from "@/context/page-context";
import { ErrorPage } from "@/pages/ErrorPage";

export function RouteContent({ Page }) {
    const { page } = usePage();
    return page === "error" ? <ErrorPage /> : <Page />;
}
