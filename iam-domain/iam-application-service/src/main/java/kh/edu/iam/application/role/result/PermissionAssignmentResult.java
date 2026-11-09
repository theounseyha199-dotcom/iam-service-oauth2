package kh.edu.iam.application.role.result;

import kh.edu.iam.RolePermission;

import java.time.Instant;
import java.util.UUID;

public record PermissionAssignmentResult(
        Long assignmentId,
        UUID roleUuid,
        UUID permissionUuid,
        Instant assignedAt,
        String assignedBy,
        Instant expiresAt,
        String reason
) {

    public static PermissionAssignmentResult from(
            RolePermission assignment,
            UUID roleUuid,
            UUID permissionUuid
    ) {
        return new PermissionAssignmentResult(
                assignment.getId(),
                roleUuid,
                permissionUuid,
                assignment.getAssignedAt(),
                assignment.getAssignedBy(),
                assignment.getExpiresAt(),
                assignment.getReason()
        );
    }
}