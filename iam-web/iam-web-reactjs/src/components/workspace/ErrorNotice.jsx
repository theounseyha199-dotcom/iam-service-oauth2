import { useWorkspace } from "@/context/workspace-context";
import { Notice } from "@/components/Notice";
import { AppLink } from "@/components/AppLink";

export function ErrorNotice({ error }) {
    const { t, url } = useWorkspace();
    if (!error) return null;
    return (
        <Notice as="div">
            <p>
                {t(
                    error.status === 401
                        ? "expired"
                        : error.status === 403
                          ? "forbidden"
                          : error.status === 409
                            ? "conflict"
                            : error.status === 400
                              ? "invalid"
                              : "error",
                )}
            </p>
            {error.status === 401 && (
                <AppLink href={url("/login")}>{t("login")}</AppLink>
            )}
            {error.fields?.length > 0 && (
                <ul>
                    {error.fields.map((field) => (
                        <li key={field}>{t(field)}</li>
                    ))}
                </ul>
            )}
        </Notice>
    );
}
