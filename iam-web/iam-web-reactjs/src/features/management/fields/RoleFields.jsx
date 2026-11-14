import { Input } from "@/components/workspace/WorkspaceInput";
export function RoleFields({ row }) {
    return (
        <>
            <Input name="name" value={row.name} required />
            <Input name="description" value={row.description} />
        </>
    );
}
