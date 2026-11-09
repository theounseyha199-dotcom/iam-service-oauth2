package kh.edu.iam.application.permission.result;

import kh.edu.iam.Permission;

import java.time.Instant;
import java.util.UUID;

public record PermissionResult(
        UUID uuid,
        String name,
        String resource,
        String action,
        String description,
        boolean system,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static PermissionResult from(Permission permission) {
        return new PermissionResult(
                permission.getUuid(),
                permission.getName(),
                permission.getResource(),
                permission.getAction(),
                permission.getDescription(),
                permission.isSystem(),
                permission.isActive(),
                permission.getAudit().createdAt(),
                permission.getAudit().updatedAt(),
                permission.getAudit().version());
    }
}
