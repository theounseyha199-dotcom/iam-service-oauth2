package kh.edu.iam.persistence.adapter;

import kh.edu.iam.Assignment;
import kh.edu.iam.UserPermission;
import kh.edu.iam.UserRole;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.persistence.entity.UserAssignmentEntity;
import kh.edu.iam.persistence.mapper.UserPermissionPersistenceMapper;
import kh.edu.iam.persistence.mapper.UserRolePersistenceMapper;
import kh.edu.iam.persistence.repository.UserPermissionJpaRepository;
import kh.edu.iam.persistence.repository.UserRoleJpaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class UserAssignmentPersistenceAdapter implements UserAssignmentRepository {
    private final UserRoleJpaRepository roles;
    private final UserPermissionJpaRepository permissions;

    @Override
    public List<UserRole> findRoles(Long userId) {
        return roles.findByUserIdOrderById(userId).stream().map(UserRolePersistenceMapper::toDomain).toList();
    }

    @Override
    public List<UserPermission> findPermissions(Long userId) {
        return permissions.findByUserIdOrderById(userId).stream()
                .map(UserPermissionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public UserRole insertRole(UserRole assignment) {
        return UserRolePersistenceMapper.toDomain(
                roles.saveAndFlush(UserRolePersistenceMapper.toNewEntity(assignment)));
    }

    @Override
    public UserPermission insertPermission(UserPermission assignment) {
        return UserPermissionPersistenceMapper.toDomain(
                permissions.saveAndFlush(UserPermissionPersistenceMapper.toNewEntity(assignment)));
    }

    @Override
    public void saveRoleRevocation(UserRole a, String reason) {
        var entity = roles.findForUpdate(a.getId()).orElseThrow();
        if (!entity.getRoleId().equals(a.getRoleId())) {
            throw new IllegalArgumentException("Role mismatch");
        }
        applyRevocation(a, entity, reason);
        roles.saveAndFlush(entity);
    }

    @Override
    public void savePermissionRevocation(UserPermission a, String reason) {
        var entity = permissions.findForUpdate(a.getId()).orElseThrow();
        if (!entity.getPermissionId().equals(a.getPermissionId()) || entity.getEffect() != a.getEffect()) {
            throw new IllegalArgumentException("Permission mismatch");
        }
        applyRevocation(a, entity, reason);
        permissions.saveAndFlush(entity);
    }

    private static void applyRevocation(Assignment a, UserAssignmentEntity e, String reason) {
        if (!e.getUserId().equals(a instanceof UserRole r ? r.getUserId() : ((UserPermission) a).getUserId())) {
            throw new IllegalArgumentException("User mismatch");
        }
        if (e.getRevokedAt() != null) {
            throw new IllegalStateException("Assignment already revoked");
        }
        if (a.getRevokedAt() == null || a.getRevokedBy() == null) {
            throw new IllegalArgumentException("Revocation is required");
        }
        if (reason != null && reason.length() > 255) {
            throw new IllegalArgumentException("Reason is too long");
        }
        e.setRevokedAt(a.getRevokedAt());
        e.setRevokedBy(a.getRevokedBy());
        e.setRevocationReason(reason);
    }
}
