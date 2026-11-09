package kh.edu.iam.persistence.repository;

import jakarta.persistence.LockModeType;
import kh.edu.iam.persistence.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolePermissionJpaRepository
        extends JpaRepository<RolePermissionEntity, Long> {

    List<RolePermissionEntity> findByRoleIdOrderByAssignedAtDescIdDesc(
            Long roleId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select assignment
            from RolePermissionEntity assignment
            where assignment.roleId = :roleId
              and assignment.revokedAt is null
              and assignment.permissionId in (
                  select permission.id
                  from PermissionEntity permission
                  where permission.uuid = :permissionUuid
              )
            """)
    List<RolePermissionEntity> findUnrevokedForUpdate(
            @Param("roleId") Long roleId,
            @Param("permissionUuid") UUID permissionUuid
    );

    @Query("""
            select assignment
            from RolePermissionEntity assignment
            where assignment.roleId = :roleId
              and assignment.assignedAt <= :now
              and (
                  assignment.revokedAt is null
                  or assignment.revokedAt > :now
              )
              and (
                  assignment.expiresAt is null
                  or assignment.expiresAt > :now
              )
            order by assignment.id
            """)
    List<RolePermissionEntity> findActiveByRoleId(
            @Param("roleId") Long roleId,
            @Param("now") Instant now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select assignment
            from RolePermissionEntity assignment
            where assignment.id = :id
            """)
    Optional<RolePermissionEntity> findByIdForUpdate(
            @Param("id") Long id
    );
}

/*
These methods support:

- Viewing assignment history.
- Loading currently effective assignments.
- Locking an existing assignment during revocation.

 */
