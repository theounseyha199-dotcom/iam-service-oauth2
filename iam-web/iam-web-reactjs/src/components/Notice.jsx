import { CircleCheck, TriangleAlert } from "lucide-react";
import { cn } from "cn";

/** Shared feedback for native account forms and the JSON-backed workspace. */
export function Notice({
    message,
    success = false,
    children,
    as: Container = "p",
    className,
    ...props
}) {
    if (!message && !children) return null;
    const Icon = success ? CircleCheck : TriangleAlert;
    const Content = Container === "p" ? "span" : "div";
    return (
        <Container
            className={cn(
                "notice iam-notice my-4! flex items-start gap-[.65rem] rounded-lg border px-4 py-3.5 forced-colors:border-[CanvasText]",
                success
                    ? "success border-green-600 bg-green-50 text-slate-900"
                    : "danger border-destructive bg-red-50 text-destructive",
                className,
            )}
            role={success ? "status" : "alert"}
            tabIndex={success ? undefined : -1}
            {...props}
        >
            <Icon
                className="iam-notice-icon mt-[.2em] size-[18px] shrink-0"
                aria-hidden="true"
                focusable="false"
            />
            <Content className="iam-notice-content min-w-0 flex-1 wrap-anywhere [&>p:first-child]:mt-0 [&>p:last-child]:mb-0">
                {message}
                {children}
            </Content>
        </Container>
    );
}
