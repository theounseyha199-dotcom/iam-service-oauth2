package kh.edu.iam.application.port.out;

import kh.edu.iam.Permission;
import kh.edu.iam.Role;
import kh.edu.iam.RolePermission;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolePermissionRepository {

    Optional<Role> lockRole(UUID roleUuid);

    Optional<Permission> lockPermission(UUID permissionUuid);

    List<RolePermission> findActiveByRoleId(
            Long roleId,
            Instant now
    );

    RolePermission insert(RolePermission assignment);

    void saveRevocation(
            RolePermission assignment,
            String revocationReason
    );

    Optional<RolePermission> findUnrevokedForUpdate(
            Long roleId,
            UUID permissionUuid,
            Instant now
    );
}
