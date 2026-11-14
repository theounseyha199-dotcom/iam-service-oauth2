import { Input } from "@/components/workspace/WorkspaceInput";
export function PermissionFields({ row }) {
    return (
        <>
            <Input name="resource" value={row.resource} required />
            <Input name="action" value={row.action} required />
            <Input name="description" value={row.description} />
        </>
    );
}
