import { usePage } from "@/context/page-context";
import { ProfileAvatar } from "@/components/ProfileAvatar";

export function ProfileSummary() {
    const { data, t } = usePage();
    const claims = data.claims ?? {};
    const name = [claims.givenName, claims.familyName]
        .filter(Boolean)
        .join(" ");
    return (
        <section
            aria-label={t("profile.summary")}
            className="mb-6 flex flex-col items-start gap-5 sm:flex-row sm:items-center rounded-xl border border-border bg-background p-5"
        >
            <ProfileAvatar
                src={claims.pictureUrl}
                name={name}
                alt={t("profile.pictureAlt")}
            />
            <div className="w-full min-w-0 sm:w-auto sm:flex-1">
                <h2>{name}</h2>
                <dl className="grid gap-x-6 sm:grid-cols-2">
                    {[
                        ["username", claims.username],
                        ["email", claims.email],
                        ["phoneNumber", claims.phoneNumber],
                    ].map(([key, value]) => (
                        <div key={key}>
                            <dt>{t(`field.${key}`)}</dt>
                            <dd>{value || t("common.notProvided")}</dd>
                        </div>
                    ))}
                </dl>
            </div>
        </section>
    );
}
