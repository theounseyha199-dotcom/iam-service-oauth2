import { Input } from "@/components/workspace/WorkspaceInput";
export function UserFields({ row, fresh }) {
    return (
        <>
            <Input
                name="username"
                value={row.username}
                required
                readOnly={false}
            />
            <Input name="email" type="email" value={row.email} required />
            <Input name="givenName" value={row.profile?.givenName} required />
            <Input name="familyName" value={row.profile?.familyName} required />
            {fresh ? (
                <Input
                    name="password"
                    type="password"
                    required
                    hint="passwordHint"
                />
            ) : (
                <>
                    <Input
                        name="phoneNumber"
                        type="tel"
                        value={row.profile?.phoneNumber}
                    />
                    <Input
                        name="pictureUrl"
                        type="url"
                        value={row.profile?.pictureUrl}
                    />
                </>
            )}
        </>
    );
}
