import { useId, useState } from "react";
import { useFormContext } from "react-hook-form";
import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { ResourceState } from "@/components/workspace/ResourceState";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Pager } from "@/components/workspace/Pager";

export function AssignmentPicker({ kind, field }) {
    const { t } = useWorkspace();
    const { setValue } = useFormContext();
    const id = useId();
    const [draft, setDraft] = useState("");
    const [search, setSearch] = useState("");
    const [page, setPage] = useState(0);
    const resource = useResource(
        `/${kind}?size=20&page=${page}&search=${encodeURIComponent(search)}`,
    );
    function find() {
        setSearch(draft.trim());
        setPage(0);
    }
    return (
        <div className="col-span-full my-3 grid min-w-0 gap-3 rounded-lg border border-border p-4">
            <label htmlFor={`${id}-search`}>{t("recordSearch")}</label>
            <div className="flex gap-2">
                <Input
                    id={`${id}-search`}
                    value={draft}
                    onChange={(event) => setDraft(event.target.value)}
                    onKeyDown={(event) => {
                        if (
                            event.key === "Enter" &&
                            !event.nativeEvent.isComposing
                        ) {
                            event.preventDefault();
                            find();
                        }
                    }}
                />
                <Button type="button" variant="outline" onClick={find}>
                    {t("search")}
                </Button>
            </div>
            <ResourceState resource={resource}>
                {(result) => (
                    <>
                        <label htmlFor={`${id}-record`}>
                            {t("selectRecord")}
                        </label>
                        <select
                            key={`${search}-${page}`}
                            id={`${id}-record`}
                            defaultValue=""
                            onChange={(event) => {
                                if (event.target.value)
                                    setValue(field, event.target.value, {
                                        shouldDirty: true,
                                        shouldValidate: true,
                                    });
                            }}
                        >
                            <option value="">{t("selectRecord")}</option>
                            {result.content.map((record) => (
                                <option key={record.uuid} value={record.uuid}>
                                    {record.name} · {record.uuid.slice(0, 8)}
                                </option>
                            ))}
                        </select>
                        <Pager metadata={result.page} onPage={setPage} />
                    </>
                )}
            </ResourceState>
        </div>
    );
}
