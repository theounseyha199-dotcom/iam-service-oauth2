package kh.edu.iam.application.user.result;

import kh.edu.iam.UserRole;

import java.time.Instant;
import java.util.UUID;

public record RoleAssignmentResult(
        Long assignmentId,
        UUID roleUuid,
        String name,
        Instant assignedAt,
        String assignedBy,
        Instant expiresAt,
        String reason) {
    public static RoleAssignmentResult from(UserRole a, UUID uuid, String name) {
        return new RoleAssignmentResult(
                a.getId(), uuid, name, a.getAssignedAt(), a.getAssignedBy(), a.getExpiresAt(), a.getReason());
    }

    public UUID uuid() {
        return roleUuid;
    }
}
