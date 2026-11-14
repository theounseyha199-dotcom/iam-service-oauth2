import { useCallback, useEffect, useRef } from "react";
import { flushSync } from "react-dom";
import { useBeforeUnload, useBlocker } from "react-router";
import { Button } from "@/components/ui/button";
import { usePage } from "@/context/page-context";

/** Covers Router links, Back/Forward, native protocol links and actual tab closure. */
export function UnsavedChangesGuard({ dirty, setDirty, children }) {
    const { t } = usePage();
    const blocker = useBlocker(dirty);
    const dialog = useRef(null);
    const nativeDestination = useRef(null);
    const originalFocus = useRef(null);
    useBeforeUnload(
        useCallback(
            (event) => {
                if (dirty) {
                    event.preventDefault();
                    event.returnValue = "";
                }
            },
            [dirty],
        ),
    );
    useEffect(() => {
        if (blocker.state === "blocked" && !dialog.current.open) {
            originalFocus.current = document.activeElement;
            dialog.current.showModal();
            dialog.current.querySelector("button").focus();
        }
    }, [blocker]);
    function capture(event) {
        const link = event.target.closest("a[href]");
        if (
            !dirty ||
            !link ||
            event.ctrlKey ||
            event.metaKey ||
            event.shiftKey ||
            event.altKey ||
            event.button !== 0 ||
            link.target === "_blank"
        )
            return;
        if (link.hasAttribute("data-router-link")) return;
        event.preventDefault();
        nativeDestination.current = link.href;
        originalFocus.current = link;
        dialog.current.showModal();
        dialog.current.querySelector("button").focus();
    }
    function stay() {
        if (blocker.state === "blocked") blocker.reset();
        nativeDestination.current = null;
        dialog.current.close();
    }
    function leave() {
        flushSync(() => setDirty(false));
        dialog.current.close();
        if (blocker.state === "blocked") blocker.proceed();
        else window.location.assign(nativeDestination.current);
    }
    return (
        <>
            <div onClickCapture={capture}>{children}</div>
            <dialog
                ref={dialog}
                id="unsaved-dialog"
                aria-labelledby="unsaved-title"
                aria-describedby="unsaved-description"
                className="m-auto max-w-[min(34rem,calc(100vw-2rem))] rounded-2xl border border-border bg-card p-6 text-foreground backdrop:bg-slate-900/45 [&_h2]:text-xl [&_h2]:font-semibold [&_p]:my-3"
                onCancel={(event) => {
                    event.preventDefault();
                    stay();
                }}
                onClose={() => originalFocus.current?.focus()}
            >
                <h2 id="unsaved-title">{t("common.unsavedTitle")}</h2>
                <p id="unsaved-description">{t("common.unsavedDescription")}</p>
                <div className="actions mt-5 flex flex-wrap items-end justify-end gap-3">
                    <Button type="button" onClick={stay}>
                        {t("common.stay")}
                    </Button>
                    <Button type="button" onClick={leave}>
                        {t("common.leave")}
                    </Button>
                </div>
            </dialog>
        </>
    );
}
