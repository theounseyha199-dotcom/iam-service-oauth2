package kh.edu.iam.persistence.mapper;

import kh.edu.iam.RolePermission;
import kh.edu.iam.persistence.entity.RolePermissionEntity;

import java.util.Objects;

public final class RolePermissionPersistenceMapper {

    private RolePermissionPersistenceMapper() {
    }

    public static RolePermissionEntity toNewEntity(
            RolePermission assignment
    ) {
        Objects.requireNonNull(assignment, "assignment is required");

        if (assignment.getId() != null) {
            throw new IllegalArgumentException(
                    "A new assignment must not have an ID"
            );
        }

        RolePermissionEntity entity = new RolePermissionEntity();

        entity.setRoleId(assignment.getRoleId());
        entity.setPermissionId(assignment.getPermissionId());

        entity.setAssignedAt(assignment.getAssignedAt());
        entity.setAssignedBy(assignment.getAssignedBy());
        entity.setExpiresAt(assignment.getExpiresAt());

        entity.setRevokedAt(assignment.getRevokedAt());
        entity.setRevokedBy(assignment.getRevokedBy());
        entity.setReason(assignment.getReason());

        return entity;
    }

    public static RolePermission toDomain(
            RolePermissionEntity entity
    ) {
        Objects.requireNonNull(entity, "entity is required");

        return new RolePermission(
                entity.getId(),
                entity.getRoleId(),
                entity.getPermissionId(),
                entity.getAssignedAt(),
                entity.getAssignedBy(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getRevokedBy(),
                entity.getReason()
        );
    }
}
