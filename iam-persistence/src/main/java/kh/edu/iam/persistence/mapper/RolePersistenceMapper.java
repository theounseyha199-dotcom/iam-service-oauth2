package kh.edu.iam.persistence.mapper;

import kh.edu.iam.AuditMetadata;
import kh.edu.iam.DeletionMetadata;
import kh.edu.iam.Role;
import kh.edu.iam.persistence.entity.RoleEntity;

public final class RolePersistenceMapper {

    private RolePersistenceMapper() {}

    public static RoleEntity toNewEntity(Role role) {
        RoleEntity entity = new RoleEntity();

        entity.setUuid(role.getUuid());
        entity.setSystem(role.isSystem());

        entity.setCreatedAt(role.getAudit().createdAt());
        entity.setCreatedBy(role.getAudit().createdBy());

        applyChanges(role, entity);

        // JPA initializes the version when inserting.
        return entity;
    }

    public static void applyChanges(Role role, RoleEntity entity) {
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setActive(role.isActive());

        entity.setUpdatedAt(role.getAudit().updatedAt());
        entity.setUpdatedBy(role.getAudit().updatedBy());

        entity.setDeleted(role.getDeletion().deleted());
        entity.setDeletedAt(role.getDeletion().deletedAt());
        entity.setDeletedBy(role.getDeletion().deletedBy());
    }

    public static Role toDomain(RoleEntity entity) {
        return new Role(
                entity.getId(),
                entity.getUuid(),
                entity.getName(),
                entity.getDescription(),
                entity.isSystem(),
                entity.isActive(),
                new AuditMetadata(
                        entity.getCreatedAt(),
                        entity.getCreatedBy(),
                        entity.getUpdatedAt(),
                        entity.getUpdatedBy(),
                        entity.getVersion()),
                new DeletionMetadata(entity.isDeleted(), entity.getDeletedBy(), entity.getDeletedAt()));
    }
}
