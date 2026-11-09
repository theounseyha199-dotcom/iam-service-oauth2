package kh.edu.iam;

import lombok.Getter;

import java.time.Instant;

@Getter
public final class UserRole extends Assignment {

    private final Long userId;
    private final Long roleId;

    public UserRole(
            Long id,
            Long userId,
            Long roleId,
            Instant assignedAt,
            String assignedBy,
            Instant expiresAt,
            Instant revokedAt,
            String revokedBy,
            String reason
    ) {
        super(
                id, assignedAt, assignedBy, expiresAt,
                revokedAt, revokedBy, reason
        );

        this.userId = requireId(userId, "userId");
        this.roleId = requireId(roleId, "roleId");
    }

    public static UserRole assign(
            Long userId,
            Long roleId,
            String actor,
            Instant now,
            Instant expiresAt,
            String reason
    ) {
        return new UserRole(
                null, userId, roleId,
                now, actor, expiresAt,
                null, null, reason
        );
    }
}