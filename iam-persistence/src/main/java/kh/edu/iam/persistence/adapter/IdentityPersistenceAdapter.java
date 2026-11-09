package kh.edu.iam.persistence.adapter;

import jakarta.persistence.EntityManager;

import kh.edu.iam.Identity;
import kh.edu.iam.IdentityProvider;
import kh.edu.iam.application.port.out.IdentityRepository;
import kh.edu.iam.persistence.entity.IdentityEntity;
import kh.edu.iam.persistence.mapper.IdentityPersistenceMapper;
import kh.edu.iam.persistence.repository.IdentityJpaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class IdentityPersistenceAdapter implements IdentityRepository {
    private final IdentityJpaRepository repository;
    private final EntityManager entityManager;

    @Override
    public void lockProviderSubject(IdentityProvider provider, String subject) {
        // Transaction-scoped lock also serializes provisioning when no identity row exists yet.
        entityManager
                .createNativeQuery("select pg_advisory_xact_lock(hashtextextended(:key, 0))")
                .setParameter("key", provider.name() + ":" + subject)
                .getSingleResult();
    }

    @Override
    public Optional<Identity> findByProviderSubject(IdentityProvider provider, String subject) {
        return repository.findByProviderAndProviderSubject(provider, subject).map(IdentityPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Identity> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid).map(IdentityPersistenceMapper::toDomain);
    }

    @Override
    public List<Identity> findByUserId(Long userId) {
        return repository.findByUserIdAndDeletedFalseOrderById(userId).stream()
                .map(IdentityPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Identity save(Identity identity) {
        IdentityEntity entity;
        if (identity.getId() == null) {
            entity = new IdentityEntity();
        } else {
            entity = repository.findById(identity.getId()).orElseThrow();
            if (!entity.getUuid().equals(identity.getUuid())) {
                throw new IllegalArgumentException("Identity ID mismatch");
            }
            if (!Objects.equals(entity.getVersion(), identity.getAudit().version())) {
                throw new ObjectOptimisticLockingFailureException(IdentityEntity.class, identity.getId());
            }
        }
        IdentityPersistenceMapper.apply(identity, entity);
        return IdentityPersistenceMapper.toDomain(repository.saveAndFlush(entity));
    }
}
