package kh.edu.iam.persistence.adapter;

import kh.edu.iam.Role;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.persistence.entity.RoleEntity;
import kh.edu.iam.persistence.mapper.RolePersistenceMapper;
import kh.edu.iam.persistence.repository.RoleJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class RolePersistenceAdapter implements RoleRepository {

    private final RoleJpaRepository repository;

    public RolePersistenceAdapter(RoleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Role save(Role role) {
        Objects.requireNonNull(role, "role is required");

        RoleEntity entity;

        if (role.getId() == null) {
            entity = RolePersistenceMapper.toNewEntity(role);
        } else {
            entity = repository.findById(role.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Role no longer exists"
                    ));

            if (!Objects.equals(entity.getUuid(), role.getUuid())) {
                throw new IllegalArgumentException(
                        "Role ID and UUID do not match"
                );
            }

            if (!Objects.equals(
                    entity.getVersion(),
                    role.getAudit().version()
            )) {
                throw new ObjectOptimisticLockingFailureException(
                        RoleEntity.class,
                        role.getId()
                );
            }

            RolePersistenceMapper.applyChanges(role, entity);
        }

        try {
            RoleEntity saved = repository.saveAndFlush(entity);
            return RolePersistenceMapper.toDomain(saved);
        } catch (DataIntegrityViolationException exception) {
            if (isNameConflict(exception)) {
                throw new DuplicateResourceException(
                        "Role already exists: " + role.getName()
                );
            }

            throw exception;
        }
    }

    @Override
    public Optional<Role> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid)
                .map(RolePersistenceMapper::toDomain);
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
                return "uk_roles_name".equals(
                        violation.getConstraintName()
                );
            }

            current = current.getCause();
        }

        return false;
    }
}