package kh.edu.iam.application.role.result;

import kh.edu.iam.Role;

import java.time.Instant;
import java.util.UUID;

public record RoleResult(
        UUID uuid,
        String name,
        String description,
        boolean system,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static RoleResult from(Role role) {
        return new RoleResult(
                role.getUuid(),
                role.getName(),
                role.getDescription(),
                role.isSystem(),
                role.isActive(),
                role.getAudit().createdAt(),
                role.getAudit().updatedAt(),
                role.getAudit().version());
    }
}
