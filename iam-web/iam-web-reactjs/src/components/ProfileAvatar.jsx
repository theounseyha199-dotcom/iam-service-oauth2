import { useState } from "react";
import { cn } from "cn";

function safePictureUrl(value) {
    try {
        const url = new URL(value);
        return ["https:", "http:"].includes(url.protocol) &&
            !url.username &&
            !url.password
            ? url.href
            : "";
    } catch {
        return "";
    }
}

export function ProfileAvatar({ src, name = "", alt = "", className }) {
    const url = safePictureUrl(src);
    const [failed, setFailed] = useState("");
    const initials =
        name
            .trim()
            .split(/\s+/)
            .slice(0, 2)
            .map((part) => part[0])
            .join("") || "I";
    return (
        <span
            className={cn(
                "inline-grid size-16 shrink-0 place-items-center overflow-hidden rounded-full bg-accent text-xl font-semibold text-primary",
                className,
            )}
        >
            {url && failed !== url ? (
                <img
                    src={url}
                    alt={alt}
                    referrerPolicy="no-referrer"
                    className="size-full object-cover"
                    onError={() => setFailed(url)}
                />
            ) : (
                <span aria-label={alt || undefined}>{initials}</span>
            )}
        </span>
    );
}
