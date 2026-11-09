package kh.edu.iam.persistence.mapper;

import kh.edu.iam.Identity;
import kh.edu.iam.persistence.entity.IdentityEntity;

public final class IdentityPersistenceMapper {
    private IdentityPersistenceMapper() {}

    public static void apply(Identity identity, IdentityEntity entity) {
        entity.setUuid(identity.getUuid());
        entity.setUserId(identity.getUserId());
        entity.setProvider(identity.getProvider());
        entity.setProviderSubject(identity.getProviderSubject());
        entity.setEmail(identity.getEmail());
        entity.setEmailVerified(identity.isEmailVerified());
        entity.setDisplayName(identity.getDisplayName());
        entity.setUsername(identity.getUsername());
        entity.setPictureUrl(identity.getPictureUrl());
        entity.setAttributes(identity.getAttributes());
        entity.setLinkedAt(identity.getLinkedAt());
        entity.setLastLoginAt(identity.getLastLoginAt());
        AuditPersistenceMapper.apply(entity, identity.getAudit(), identity.getDeletion());
    }

    public static Identity toDomain(IdentityEntity entity) {
        return new Identity(
                entity.getId(),
                entity.getUuid(),
                entity.getUserId(),
                entity.getProvider(),
                entity.getProviderSubject(),
                entity.getEmail(),
                entity.isEmailVerified(),
                entity.getDisplayName(),
                entity.getUsername(),
                entity.getPictureUrl(),
                entity.getAttributes(),
                entity.getLinkedAt(),
                entity.getLastLoginAt(),
                AuditPersistenceMapper.audit(entity),
                AuditPersistenceMapper.deletion(entity));
    }
}
