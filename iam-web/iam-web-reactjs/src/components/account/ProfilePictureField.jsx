import { usePage } from "@/context/page-context";
import { useState } from "react";
import { Field } from "@/components/account/Field";
import { ProfileAvatar } from "@/components/ProfileAvatar";

export function ProfilePictureField() {
    const { data, t } = usePage();
    const [picture, setPicture] = useState(data.values?.pictureUrl ?? "");
    return (
        <div className="min-w-0">
            <Field
                name="pictureUrl"
                type="url"
                maxLength={500}
                onValueChange={setPicture}
            />
            <div
                className="flex items-center gap-3"
                aria-label={t("profile.picturePreview")}
            >
                <ProfileAvatar
                    src={picture}
                    name={[data.values?.givenName, data.values?.familyName]
                        .filter(Boolean)
                        .join(" ")}
                    alt={t("profile.picturePreview")}
                    className="size-12 text-base"
                />
                <p className="text-xs text-muted-foreground">
                    {t("profile.pictureHint")}
                </p>
            </div>
        </div>
    );
}
