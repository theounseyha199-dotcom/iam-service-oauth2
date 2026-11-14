import { useWorkspace } from "@/context/workspace-context";
import { useRef, useState, useEffect } from "react";
import { Input as ShadcnInput } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export function Search({ value, onChange }) {
    const { t } = useWorkspace();
    const input = useRef(null);
    const [draft, setDraft] = useState(value);
    const composing = useRef(false);
    const timer = useRef(null);
    useEffect(() => () => clearTimeout(timer.current), []);
    function schedule(value) {
        clearTimeout(timer.current);
        timer.current = setTimeout(() => onChange(value), 300);
    }
    return (
        <form
            className="workspace-search"
            noValidate
            onSubmit={(e) => {
                e.preventDefault();
                if (!composing.current) {
                    clearTimeout(timer.current);
                    onChange(draft);
                }
            }}
        >
            <label htmlFor="workspace-search">{t("search")}</label>
            <div className="input-row flex items-stretch gap-2 [&_input]:flex-1 [.dashboard-root_&]:items-center">
                <ShadcnInput
                    ref={input}
                    id="workspace-search"
                    type="search"
                    value={draft}
                    onCompositionStart={() => {
                        composing.current = true;
                        clearTimeout(timer.current);
                    }}
                    onCompositionEnd={(e) => {
                        composing.current = false;
                        schedule(e.currentTarget.value);
                    }}
                    onChange={(e) => {
                        setDraft(e.target.value);
                        if (!composing.current) schedule(e.target.value);
                    }}
                />
                {draft && (
                    <Button
                        type="button"
                        variant="outline"
                        aria-label={t("clear")}
                        onClick={() => {
                            clearTimeout(timer.current);
                            setDraft("");
                            onChange("");
                            input.current.focus();
                        }}
                    >
                        ×
                    </Button>
                )}
            </div>
        </form>
    );
}
