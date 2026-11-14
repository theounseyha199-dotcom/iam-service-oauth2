import { useEffect, useRef, useState } from "react";
export function useWorkspaceConfirmation() {
    const [pending, setPending] = useState(null),
        [busy, setBusy] = useState(false),
        [error, setError] = useState(null);
    const dialogRef = useRef(null),
        trigger = useRef(null),
        running = useRef(false);
    useEffect(() => {
        if (pending) {
            dialogRef.current.showModal();
            dialogRef.current.querySelector("button").focus();
        }
    }, [pending]);
    function confirm(action) {
        trigger.current = document.activeElement;
        setPending(action);
        setError(null);
    }
    function onCancel() {
        if (running.current) return;
        dialogRef.current.close();
        setPending(null);
        trigger.current?.focus();
    }
    async function onCommit() {
        if (running.current) return;
        running.current = true;
        setBusy(true);
        try {
            await pending.run();
            running.current = false;
            onCancel();
        } catch (failure) {
            setError(failure);
        } finally {
            running.current = false;
            setBusy(false);
        }
    }
    return { confirm, pending, busy, error, dialogRef, onCancel, onCommit };
}
