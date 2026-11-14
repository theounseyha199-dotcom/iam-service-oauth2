import { ProfileSummary } from "@/components/account/ProfileSummary";
import { Link } from "@/components/account/AccountLink";
import { Form } from "@/components/account/AccountForm";
import { Hidden } from "@/components/account/Hidden";
import { profileFields } from "@/context/account-config";
import { ProfilePictureField } from "@/components/account/ProfilePictureField";
import { SelectField } from "@/components/account/SelectField";
import { Field } from "@/components/account/Field";
import { Submit } from "@/components/account/Submit";
import { usePage } from "@/context/page-context";

export function ProfileEditPage() {
    const { data: d, t } = usePage();
    return (
        <>
            <ProfileSummary />
            <p>{t("profile.editHint")}</p>
            {d.errorCode === "concurrent_update" && (
                <Link to="/profile/edit" label="common.reload" />
            )}
            <Form noValidate action="/profile/edit" dirty>
                <Hidden name="version" value={d.values?.version ?? "-1"} />
                <div className="form-grid grid grid-cols-2 gap-x-5 gap-y-3 max-[700px]:grid-cols-1 [.dashboard-root_&]:max-md:grid-cols-1">
                    {profileFields.map((name) =>
                        name === "pictureUrl" ? (
                            <ProfilePictureField key={name} />
                        ) : name === "gender" ? (
                            <SelectField
                                key={name}
                                name={name}
                                choices={[
                                    ["", "common.notProvided"],
                                    ...["female", "male", "other"].map((g) => [
                                        g,
                                        `gender.${g}`,
                                    ]),
                                ]}
                            />
                        ) : (
                            <Field
                                key={name}
                                name={name}
                                type={
                                    name === "email"
                                        ? "email"
                                        : name === "birthdate"
                                          ? "date"
                                          : name.endsWith("Url")
                                            ? "url"
                                            : name === "phoneNumber"
                                              ? "tel"
                                              : "text"
                                }
                                required={[
                                    "username",
                                    "email",
                                    "givenName",
                                    "familyName",
                                ].includes(name)}
                                maxLength={
                                    name === "username"
                                        ? 50
                                        : name.endsWith("Url")
                                          ? 500
                                          : ["email", "streetAddress"].includes(
                                                  name,
                                              )
                                            ? 255
                                            : 100
                                }
                            />
                        ),
                    )}
                    <SelectField
                        name="locale"
                        choices={[
                            ["en", "common.english"],
                            ["km", "common.khmer"],
                        ]}
                    />
                </div>
                <Submit label="common.save" />
            </Form>
        </>
    );
}
