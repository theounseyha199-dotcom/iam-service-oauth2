package kh.edu.iam.persistence.mapper;

import kh.edu.iam.UserPermission;
import kh.edu.iam.persistence.entity.UserPermissionEntity;

public final class UserPermissionPersistenceMapper {
    private UserPermissionPersistenceMapper() {}

    public static UserPermission toDomain(UserPermissionEntity entity) {
        return new UserPermission(
                entity.getId(),
                entity.getUserId(),
                entity.getPermissionId(),
                entity.getEffect(),
                entity.getAssignedAt(),
                entity.getAssignedBy(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getRevokedBy(),
                entity.getReason());
    }

    public static UserPermissionEntity toNewEntity(UserPermission assignment) {
        if (assignment.getId() != null) {
            throw new IllegalArgumentException("Assignment is already persisted");
        }
        var entity = new UserPermissionEntity();
        entity.setUserId(assignment.getUserId());
        entity.setPermissionId(assignment.getPermissionId());
        entity.setEffect(assignment.getEffect());
        entity.setAssignedAt(assignment.getAssignedAt());
        entity.setAssignedBy(assignment.getAssignedBy());
        entity.setExpiresAt(assignment.getExpiresAt());
        entity.setRevokedAt(assignment.getRevokedAt());
        entity.setRevokedBy(assignment.getRevokedBy());
        entity.setReason(assignment.getReason());
        return entity;
    }
}
