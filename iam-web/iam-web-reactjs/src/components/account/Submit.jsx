import { usePage } from "@/context/page-context";
import { useContext } from "react";
import { FormBusyContext } from "@/context/form-busy-context";
import { Button } from "@/components/ui/button";

export function Submit({ label, danger = false }) {
    const { t } = usePage();
    const busy = useContext(FormBusyContext);
    return (
        <Button
            type="submit"
            disabled={busy}
            size="account"
            variant={danger ? "destructive" : "default"}
            className={
                danger
                    ? "danger-button bg-destructive border-destructive hover:bg-slate-900"
                    : ""
            }
        >
            {t(label)}
        </Button>
    );
}
