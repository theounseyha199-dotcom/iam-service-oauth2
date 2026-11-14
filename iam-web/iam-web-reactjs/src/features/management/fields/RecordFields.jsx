import { UserFields } from "./UserFields";
import { RoleFields } from "./RoleFields";
import { PermissionFields } from "./PermissionFields";
import { ClientFields } from "./ClientFields";
export function RecordFields({ kind, row, fresh }) {
    switch (kind) {
        case "users":
            return <UserFields row={row} fresh={fresh} />;
        case "roles":
            return <RoleFields row={row} />;
        case "permissions":
            return <PermissionFields row={row} fresh={fresh} />;
        default:
            return <ClientFields row={row} />;
    }
}
