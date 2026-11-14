import { usePage } from "@/context/page-context";

export function ScopeList({ scopes = [] }) {
    const { t } = usePage();
    return (
        <ul>
            {[...scopes].sort().map((s) => (
                <li key={s}>{t(`scope.${s}`)}</li>
            ))}
        </ul>
    );
}
