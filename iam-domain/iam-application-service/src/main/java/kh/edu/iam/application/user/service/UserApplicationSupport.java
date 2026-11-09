package kh.edu.iam.application.user.service;

import kh.edu.iam.Permission;
import kh.edu.iam.Role;
import kh.edu.iam.RolePermission;
import kh.edu.iam.User;
import kh.edu.iam.UserAuthoritiesCalculator;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.result.AuthUserResult;
import kh.edu.iam.application.user.result.PermissionGrantResult;
import kh.edu.iam.application.user.result.RoleAssignmentResult;
import kh.edu.iam.application.user.result.UserAuthoritiesResult;
import kh.edu.iam.application.user.result.UserResult;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class UserApplicationSupport {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final UserAssignmentRepository assignments;
    private final RolePermissionRepository roleAssignments;
    private final Clock clock;

    public User require(UUID uuid) {
        return users.findByUuid(Objects.requireNonNull(uuid))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + uuid));
    }

    public User lock(UUID uuid) {
        return users.lockByUuid(Objects.requireNonNull(uuid))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + uuid));
    }

    public void checkUnique(User user) {
        if (users.existsByUsername(user.getUsername()) || users.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Username or email already exists");
        }
        // Avoid ambiguous username-or-email authentication lookups.
        if (user.getUsername().contains("@")) {
            throw new IllegalArgumentException("Username cannot contain @");
        }
    }

    public UserAuthoritiesResult authorities(User user) {
        var now = clock.instant();
        var userRoles = assignments.findRoles(user.getId());
        var userPermissions = assignments.findPermissions(user.getId());
        Map<Long, Role> roleMap = new HashMap<>();
        Map<Long, Permission> permissionMap = new HashMap<>();
        List<RolePermission> rolePermissions = new ArrayList<>();
        List<RoleAssignmentResult> roleResults = new ArrayList<>();
        List<PermissionGrantResult> permissionResults = new ArrayList<>();
        for (var assignment : userRoles) {
            if (!assignment.isActiveAt(now)) {
                continue;
            }
            var role = roles.findById(assignment.getRoleId());
            if (role.isEmpty() || !role.get().isAssignable()) {
                continue;
            }
            roleMap.put(assignment.getRoleId(), role.get());
            roleResults.add(RoleAssignmentResult.from(assignment, role.get().getUuid(), role.get().getName()));
            rolePermissions.addAll(roleAssignments.findActiveByRoleId(assignment.getRoleId(), now));
        }
        for (var assignment : rolePermissions) {
            permissions
                    .findById(assignment.getPermissionId())
                    .ifPresent(permission -> permissionMap.put(permission.getId(), permission));
        }
        for (var assignment : userPermissions) {
            if (!assignment.isActiveAt(now)) {
                continue;
            }
            permissions
                    .findById(assignment.getPermissionId())
                    .filter(Permission::isAssignable)
                    .ifPresent(
                            permission -> {
                                permissionMap.put(permission.getId(), permission);
                                permissionResults.add(
                                        PermissionGrantResult.from(
                                                assignment, permission.getUuid(), permission.getName()));
                            });
        }
        // Calculator requires complete references; deleted/inactive targets contribute nothing.
        var validUr = userRoles.stream().filter(assignment -> roleMap.containsKey(assignment.getRoleId())).toList();
        var validRp =
                rolePermissions.stream()
                        .filter(assignment -> permissionMap.containsKey(assignment.getPermissionId()))
                        .toList();
        var validUp =
                userPermissions.stream()
                        .filter(assignment -> permissionMap.containsKey(assignment.getPermissionId()))
                        .toList();
        var effective =
                new UserAuthoritiesCalculator().calculate(user, roleMap, permissionMap, validUr, validRp, validUp, now);
        return new UserAuthoritiesResult(roleResults, permissionResults, effective.permissions(), effective.all());
    }

    public UserResult result(User user) {
        return UserResult.from(user, authorities(user).roles());
    }

    public AuthUserResult auth(User user) {
        return AuthUserResult.from(user, authorities(user).authorities());
    }
}
