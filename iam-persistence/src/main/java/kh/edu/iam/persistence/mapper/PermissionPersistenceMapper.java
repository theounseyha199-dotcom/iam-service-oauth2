package kh.edu.iam.persistence.mapper;

import kh.edu.iam.AuditMetadata;
import kh.edu.iam.DeletionMetadata;
import kh.edu.iam.Permission;
import kh.edu.iam.persistence.entity.PermissionEntity;
import lombok.RequiredArgsConstructor;


public final class PermissionPersistenceMapper {

    private PermissionPersistenceMapper() {
    }

    public static PermissionEntity toNewEntity(Permission permission) {
        PermissionEntity entity = new PermissionEntity();

        entity.setUuid(permission.getUuid());
        entity.setName(permission.getName());
        entity.setResource(permission.getResource());
        entity.setAction(permission.getAction());
        entity.setSystem(permission.isSystem());

        entity.setCreatedAt(permission.getAudit().createdAt());
        entity.setCreatedBy(permission.getAudit().createdBy());

        applyChanges(permission, entity);

        // Leave version null: JPA initializes it when inserting.
        return entity;
    }

    public static void applyChanges(
            Permission permission,
            PermissionEntity entity
    ) {
        entity.setDescription(permission.getDescription());
        entity.setActive(permission.isActive());

        entity.setUpdatedAt(permission.getAudit().updatedAt());
        entity.setUpdatedBy(permission.getAudit().updatedBy());

        entity.setDeleted(permission.getDeletion().deleted());
        entity.setDeletedAt(permission.getDeletion().deletedAt());
        entity.setDeletedBy(permission.getDeletion().deletedBy());
    }

    public static Permission toDomain(PermissionEntity entity) {
        return new Permission(
                entity.getId(),
                entity.getUuid(),
                entity.getResource(),
                entity.getAction(),
                entity.getDescription(),
                entity.isSystem(),
                entity.isActive(),
                new AuditMetadata(
                        entity.getCreatedAt(),
                        entity.getCreatedBy(),
                        entity.getUpdatedAt(),
                        entity.getUpdatedBy(),
                        entity.getVersion()
                ),
                new DeletionMetadata(
                        entity.isDeleted(),
                        entity.getDeletedBy(),
                        entity.getDeletedAt()
                )
        );
    }
}
