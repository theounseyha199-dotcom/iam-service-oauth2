import { useWorkspace } from "@/context/workspace-context";
import { Button } from "@/components/ui/button";

export function Pager({ metadata, onPage }) {
    const { t, language } = useWorkspace();
    if (!metadata) return null;
    const number = (n) =>
        new Intl.NumberFormat(
            language === "km" ? "km-KH-u-nu-khmr" : "en-GB",
        ).format(n);
    return (
        <div className="workspace-pager my-4 flex flex-wrap items-end justify-between gap-4">
            <span>
                {number(metadata.totalElements)} {t("records")} · {t("page")}{" "}
                {number(metadata.number + 1)} /{" "}
                {number(Math.max(1, metadata.totalPages))}
            </span>
            <div className="actions mt-5 flex flex-wrap items-end justify-end gap-3 [&_form]:m-0! [.dashboard-root_&]:items-center [.dashboard-root_&]:gap-[.65rem]">
                <Button
                    type="button"
                    variant="outline"
                    disabled={metadata.number === 0}
                    onClick={() => onPage(metadata.number - 1)}
                >
                    {t("previous")}
                </Button>
                <Button
                    type="button"
                    variant="outline"
                    disabled={metadata.number + 1 >= metadata.totalPages}
                    onClick={() => onPage(metadata.number + 1)}
                >
                    {t("next")}
                </Button>
            </div>
        </div>
    );
}
