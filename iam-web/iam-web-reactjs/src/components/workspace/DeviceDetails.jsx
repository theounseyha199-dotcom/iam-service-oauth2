import { useWorkspace } from "@/context/workspace-context";
import { describeDevice } from "@/lib/device";

export function DeviceDetails({ value }) {
    const { t } = useWorkspace();
    const device = describeDevice(value);
    return (
        <span className="grid gap-1">
            <span>
                {device.browser || t("unknownBrowser")}
                {device.os ? ` · ${device.os}` : ""}
            </span>
            <span className="text-xs text-muted-foreground">
                {t(device.kind)}
            </span>
        </span>
    );
}
