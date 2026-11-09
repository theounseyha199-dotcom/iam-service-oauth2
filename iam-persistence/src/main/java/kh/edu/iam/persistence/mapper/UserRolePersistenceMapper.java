package kh.edu.iam.persistence.mapper;

import kh.edu.iam.UserRole;
import kh.edu.iam.persistence.entity.UserRoleEntity;

public final class UserRolePersistenceMapper {
    private UserRolePersistenceMapper() {}

    public static UserRole toDomain(UserRoleEntity entity) {
        return new UserRole(
                entity.getId(),
                entity.getUserId(),
                entity.getRoleId(),
                entity.getAssignedAt(),
                entity.getAssignedBy(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getRevokedBy(),
                entity.getReason());
    }

    public static UserRoleEntity toNewEntity(UserRole assignment) {
        if (assignment.getId() != null) {
            throw new IllegalArgumentException("Assignment is already persisted");
        }
        var entity = new UserRoleEntity();
        entity.setUserId(assignment.getUserId());
        entity.setRoleId(assignment.getRoleId());

        entity.setAssignedAt(assignment.getAssignedAt());
        entity.setAssignedBy(assignment.getAssignedBy());
        entity.setExpiresAt(assignment.getExpiresAt());
        entity.setRevokedAt(assignment.getRevokedAt());
        entity.setRevokedBy(assignment.getRevokedBy());
        entity.setReason(assignment.getReason());
        return entity;
    }
}
