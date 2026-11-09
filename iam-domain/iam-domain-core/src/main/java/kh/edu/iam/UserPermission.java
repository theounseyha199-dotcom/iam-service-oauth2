package kh.edu.iam;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public final class UserPermission extends Assignment {

    private final Long userId;
    private final Long permissionId;
    private final PermissionEffect effect;

    public UserPermission(
            Long id,
            Long userId,
            Long permissionId,
            PermissionEffect effect,
            Instant assignedAt,
            String assignedBy,
            Instant expiresAt,
            Instant revokedAt,
            String revokedBy,
            String reason) {
        super(id, assignedAt, assignedBy, expiresAt, revokedAt, revokedBy, reason);

        this.userId = requireId(userId, "userId");
        this.permissionId = requireId(permissionId, "permissionId");
        this.effect = Objects.requireNonNull(effect, "effect is required");
    }

    public static UserPermission assign(
            Long userId,
            Long permissionId,
            PermissionEffect effect,
            String actor,
            Instant now,
            Instant expiresAt,
            String reason) {
        return new UserPermission(null, userId, permissionId, effect, now, actor, expiresAt, null, null, reason);
    }
}
