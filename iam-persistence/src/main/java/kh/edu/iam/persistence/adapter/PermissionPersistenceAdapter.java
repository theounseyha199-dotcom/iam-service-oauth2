package kh.edu.iam.persistence.adapter;


import kh.edu.iam.Permission;
import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.permission.query.PermissionFilterQuery;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.persistence.entity.PermissionEntity;
import kh.edu.iam.persistence.mapper.PermissionPersistenceMapper;
import kh.edu.iam.persistence.repository.PermissionJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class PermissionPersistenceAdapter implements PermissionRepository {

    private final PermissionJpaRepository repository;

    public PermissionPersistenceAdapter(
            PermissionJpaRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Permission save(Permission permission) {
        PermissionEntity entity;

        if (permission.getId() == null) {
            entity = PermissionPersistenceMapper.toNewEntity(permission);
        } else {
            entity = repository.findById(permission.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Permission no longer exists"
                    ));

            if (!Objects.equals(entity.getUuid(), permission.getUuid())) {
                throw new IllegalArgumentException(
                        "Permission ID and UUID do not match"
                );
            }

            if (!Objects.equals(
                    entity.getVersion(),
                    permission.getAudit().version()
            )) {
                throw new ObjectOptimisticLockingFailureException(
                        PermissionEntity.class,
                        permission.getId()
                );
            }

            PermissionPersistenceMapper.applyChanges(permission, entity);
        }

        try {
            PermissionEntity saved = repository.saveAndFlush(entity);
            return PermissionPersistenceMapper.toDomain(saved);
        } catch (DataIntegrityViolationException exception) {
            // Translate only the known unique-name constraint.
            if (isNameConflict(exception)) {
                throw new DuplicateResourceException(
                        "Permission already exists: " + permission.getName()
                );
            }

            throw exception;
        }
    }

    @Override
    public Optional<Permission> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid)
                .map(PermissionPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    private static boolean isNameConflict(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof
                    org.hibernate.exception.ConstraintViolationException violation) {
                return "uk_permissions_name".equals(
                        violation.getConstraintName()
                );
            }

            current = current.getCause();
        }

        return false;
    }


    @Override
    public PageResult<Permission> findAll(PermissionFilterQuery query) {
        Sort.Direction direction = query.descending()
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, query.sortBy())
                .and(Sort.by("id"));

        var pageable = PageRequest.of(
                query.page(),
                query.size(),
                sort
        );

        var page = repository.search(
                query.search(),
                query.active(),
                pageable
        );

        return new PageResult<>(
                page.getContent().stream()
                        .map(PermissionPersistenceMapper::toDomain)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
