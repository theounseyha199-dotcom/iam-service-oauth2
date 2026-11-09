package kh.edu.iam.persistence.mapper;

import kh.edu.iam.AuditMetadata;
import kh.edu.iam.DeletionMetadata;
import kh.edu.iam.persistence.entity.AuditedEntity;

public final class AuditPersistenceMapper {
    private AuditPersistenceMapper() {}

    public static AuditMetadata audit(AuditedEntity entity) {
        return new AuditMetadata(
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getVersion());
    }

    public static DeletionMetadata deletion(AuditedEntity entity) {
        return new DeletionMetadata(entity.isDeleted(), entity.getDeletedBy(), entity.getDeletedAt());
    }

    public static void apply(AuditedEntity entity, AuditMetadata audit, DeletionMetadata deletion) {
        entity.setCreatedAt(audit.createdAt());
        entity.setCreatedBy(audit.createdBy());
        entity.setUpdatedAt(audit.updatedAt());
        entity.setUpdatedBy(audit.updatedBy());
        entity.setDeleted(deletion.deleted());
        entity.setDeletedAt(deletion.deletedAt());
        entity.setDeletedBy(deletion.deletedBy());
    }
}
