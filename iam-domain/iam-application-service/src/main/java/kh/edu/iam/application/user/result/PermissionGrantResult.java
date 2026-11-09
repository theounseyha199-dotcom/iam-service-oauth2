package kh.edu.iam.application.user.result;

import kh.edu.iam.PermissionEffect;
import kh.edu.iam.UserPermission;

import java.time.Instant;
import java.util.UUID;

public record PermissionGrantResult(
        Long assignmentId,
        UUID permissionUuid,
        String name,
        PermissionEffect effect,
        Instant assignedAt,
        Instant expiresAt,
        String reason) {
    public static PermissionGrantResult from(UserPermission a, UUID uuid, String name) {
        return new PermissionGrantResult(
                a.getId(), uuid, name, a.getEffect(), a.getAssignedAt(), a.getExpiresAt(), a.getReason());
    }
}
