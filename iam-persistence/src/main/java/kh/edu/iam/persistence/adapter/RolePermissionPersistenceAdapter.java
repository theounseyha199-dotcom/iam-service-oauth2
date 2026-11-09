package kh.edu.iam.persistence.adapter;

import kh.edu.iam.Permission;
import kh.edu.iam.Role;
import kh.edu.iam.RolePermission;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.persistence.mapper.PermissionPersistenceMapper;
import kh.edu.iam.persistence.mapper.RolePermissionPersistenceMapper;
import kh.edu.iam.persistence.mapper.RolePersistenceMapper;
import kh.edu.iam.persistence.repository.PermissionJpaRepository;
import kh.edu.iam.persistence.repository.RoleJpaRepository;
import kh.edu.iam.persistence.repository.RolePermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class RolePermissionPersistenceAdapter
        implements RolePermissionRepository {

    private final RoleJpaRepository roleRepository;
    private final PermissionJpaRepository permissionRepository;
    private final RolePermissionJpaRepository assignmentRepository;

    @Override
    public Optional<Role> lockRole(UUID roleUuid) {
        return roleRepository.findForUpdate(roleUuid)
                .map(RolePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Permission> lockPermission(UUID permissionUuid) {
        return permissionRepository.findForUpdate(permissionUuid)
                .map(PermissionPersistenceMapper::toDomain);
    }

    @Override
    public List<RolePermission> findActiveByRoleId(
            Long roleId,
            Instant now
    ) {
        return assignmentRepository.findActiveByRoleId(roleId, now)
                .stream()
                .map(RolePermissionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public RolePermission insert(RolePermission assignment) {
        var entity = RolePermissionPersistenceMapper.toNewEntity(
                assignment
        );

        var saved = assignmentRepository.saveAndFlush(entity);

        return RolePermissionPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<RolePermission> findUnrevokedForUpdate(
            Long roleId,
            UUID permissionUuid,
            Instant now
    ) {
        var matches = assignmentRepository
                .findUnrevokedForUpdate(roleId, permissionUuid)
                .stream()
                .map(RolePermissionPersistenceMapper::toDomain)
                .filter(assignment -> assignment.isActiveAt(now))
                .toList();

        if (matches.size() > 1) {
            throw new IllegalStateException(
                    "Multiple active assignments exist for this role and permission"
            );
        }

        return matches.stream().findFirst();
    }

    @Override
    public void saveRevocation(
            RolePermission assignment,
            String revocationReason
    ) {
        if (assignment.getId() == null
                || assignment.getRevokedAt() == null
                || assignment.getRevokedBy() == null) {
            throw new IllegalArgumentException(
                    "A persisted, revoked assignment is required"
            );
        }

        if (revocationReason != null && revocationReason.length() > 255) {
            throw new IllegalArgumentException(
                    "revocationReason cannot exceed 255 characters"
            );
        }

        var entity = assignmentRepository
                .findByIdForUpdate(assignment.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Assignment no longer exists"
                ));

        if (!entity.getRoleId().equals(assignment.getRoleId())
                || !entity.getPermissionId().equals(
                assignment.getPermissionId()
        )) {
            throw new IllegalArgumentException(
                    "Assignment references do not match"
            );
        }

        if (entity.getRevokedAt() != null) {
            throw new IllegalStateException(
                    "Assignment has already been revoked"
            );
        }

        entity.setRevokedAt(assignment.getRevokedAt());
        entity.setRevokedBy(assignment.getRevokedBy());
        entity.setRevocationReason(revocationReason);

        assignmentRepository.saveAndFlush(entity);
    }
}