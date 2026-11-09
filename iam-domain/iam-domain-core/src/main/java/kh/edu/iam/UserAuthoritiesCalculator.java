package kh.edu.iam;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class UserAuthoritiesCalculator {

    public UserAuthorities calculate(
            User user,
            Map<Long, Role> rolesById,
            Map<Long, Permission> permissionsById,
            Collection<UserRole> userRoles,
            Collection<RolePermission> rolePermissions,
            Collection<UserPermission> userPermissions,
            Instant now
    ) {
        Objects.requireNonNull(user, "user is required");
        Objects.requireNonNull(now, "now is required");
        Objects.requireNonNull(rolesById, "rolesById is required");
        Objects.requireNonNull(
                permissionsById, "permissionsById is required"
        );
        Objects.requireNonNull(userRoles, "userRoles is required");
        Objects.requireNonNull(
                rolePermissions, "rolePermissions is required"
        );
        Objects.requireNonNull(
                userPermissions, "userPermissions is required"
        );

        Long userId = Objects.requireNonNull(
                user.getId(), "User must be persisted"
        );

        if (user.getDeletion().deleted()) {
            return new UserAuthorities(Set.of(), Set.of());
        }

        Set<Long> activeRoleIds = new HashSet<>();
        Set<String> roleAuthorities = new HashSet<>();
        Set<String> grantedPermissions = new HashSet<>();
        Set<String> deniedPermissions = new HashSet<>();

        for (UserRole assignment : userRoles) {
            if (!userId.equals(assignment.getUserId())
                    || !assignment.isActiveAt(now)) {
                continue;
            }

            Role role = requireRole(
                    rolesById, assignment.getRoleId()
            );

            if (role.isAssignable()) {
                activeRoleIds.add(role.getId());
                roleAuthorities.add(role.getAuthority());
            }
        }

        for (RolePermission assignment : rolePermissions) {
            if (!activeRoleIds.contains(assignment.getRoleId())
                    || !assignment.isActiveAt(now)) {
                continue;
            }

            Permission permission = requirePermission(
                    permissionsById,
                    assignment.getPermissionId()
            );

            if (permission.isAssignable()) {
                grantedPermissions.add(permission.getName());
            }
        }

        for (UserPermission assignment : userPermissions) {
            if (!userId.equals(assignment.getUserId())
                    || !assignment.isActiveAt(now)) {
                continue;
            }

            Permission permission = requirePermission(
                    permissionsById,
                    assignment.getPermissionId()
            );

            if (!permission.isAssignable()) {
                continue;
            }

            if (assignment.getEffect() == PermissionEffect.DENY) {
                deniedPermissions.add(permission.getName());
            } else {
                grantedPermissions.add(permission.getName());
            }
        }

        grantedPermissions.removeAll(deniedPermissions);

        return new UserAuthorities(
                roleAuthorities,
                grantedPermissions
        );
    }

    private Role requireRole(Map<Long, Role> rolesById, Long id) {
        Role role = rolesById.get(id);

        if (role == null || !id.equals(role.getId())) {
            throw new IllegalStateException(
                    "Missing or mismatched role: " + id
            );
        }

        return role;
    }

    private Permission requirePermission(
            Map<Long, Permission> permissionsById,
            Long id
    ) {
        Permission permission = permissionsById.get(id);

        if (permission == null || !id.equals(permission.getId())) {
            throw new IllegalStateException(
                    "Missing or mismatched permission: " + id
            );
        }

        return permission;
    }
}