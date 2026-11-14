export function formatAccountDate(value, { language, zone, unknown }) {
    if (!value) return unknown;
    const date = new Date(value);

    if (language !== "km")
        return new Intl.DateTimeFormat("en-GB", {
            dateStyle: "medium",
            timeStyle: "short",
            timeZone: zone,
        }).format(date);
    const parts = Object.fromEntries(
        new Intl.DateTimeFormat("en-GB", {
            year: "numeric",
            month: "numeric",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
            timeZone: zone,
        })
            .formatToParts(date)
            .map((p) => [p.type, p.value]),
    );
    const months = [
        "មករា",
        "កុម្ភៈ",
        "មីនា",
        "មេសា",
        "ឧសភា",
        "មិថុនា",
        "កក្កដា",
        "សីហា",
        "កញ្ញា",
        "តុលា",
        "វិច្ឆិកា",
        "ធ្នូ",
    ];
    return `${parts.day} ${months[Number(parts.month) - 1]} ${parts.year}, ${parts.hour}:${parts.minute}`.replace(
        /[0-9]/g,
        (n) => "០១២៣៤៥៦៧៨៩"[Number(n)],
    );
}
