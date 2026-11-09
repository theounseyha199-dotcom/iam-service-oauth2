package kh.edu.iam;

import lombok.Getter;

import java.time.Instant;

@Getter
public final class RolePermission extends Assignment {

    private final Long roleId;
    private final Long permissionId;

    public RolePermission(
            Long id,
            Long roleId,
            Long permissionId,
            Instant assignedAt,
            String assignedBy,
            Instant expiresAt,
            Instant revokedAt,
            String revokedBy,
            String reason) {
        super(id, assignedAt, assignedBy, expiresAt, revokedAt, revokedBy, reason);

        this.roleId = requireId(roleId, "roleId");
        this.permissionId = requireId(permissionId, "permissionId");
    }

    public static RolePermission assign(
            Long roleId, Long permissionId, String actor, Instant now, Instant expiresAt, String reason) {
        return new RolePermission(null, roleId, permissionId, now, actor, expiresAt, null, null, reason);
    }
}
