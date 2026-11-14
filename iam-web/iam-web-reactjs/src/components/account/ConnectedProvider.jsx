import { Link } from "@/components/account/AccountLink";
import { usePage } from "@/context/page-context";

export function ConnectedProvider({ identity }) {
    const { t, url } = usePage();
    return (
        <li>
            <div>
                <h2 className="flex items-center gap-2">
                    <img
                        src={url(`/images/${identity.provider}.svg`)}
                        width="20"
                        height="20"
                        alt=""
                    />
                    {t(`provider.${identity.provider}`)}
                </h2>
                <span className="inline-flex rounded-full bg-green-50 px-2 py-1 text-xs font-medium text-green-800">
                    {t("accounts.connected")}
                </span>
                {identity.displayName && <p>{identity.displayName}</p>}
            </div>
            <Link
                to={`/profile/accounts/${identity.uuid}/unlink`}
                button
                label="accounts.unlink"
            />
        </li>
    );
}
