package kh.edu.iam.application.role.usecase;

import kh.edu.iam.RolePermission;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.role.command.AssignPermissionsToRoleCommand;
import kh.edu.iam.application.role.result.PermissionAssignmentResult;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class AssignPermissionsToRoleUseCase {

    private final RolePermissionRepository repository;
    private final CurrentActorProvider actorProvider;
    private final TransactionRunner transactions;
    private final Clock clock;

    public List<PermissionAssignmentResult> execute(AssignPermissionsToRoleCommand command) {
        Objects.requireNonNull(command, "command is required");

        return transactions.required(() -> assign(command));
    }

    private List<PermissionAssignmentResult> assign(AssignPermissionsToRoleCommand command) {
        var role =
                repository
                        .lockRole(command.roleUuid())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + command.roleUuid()));

        if (!role.isAssignable()) {
            throw new IllegalStateException("Inactive or deleted roles cannot receive permissions");
        }

        if (role.isSystem()) {
            throw new IllegalStateException("System role permissions cannot be changed");
        }

        var now = clock.instant();
        var actor = actorProvider.currentActor();

        var assignedPermissionIds =
                repository.findActiveByRoleId(role.getId(), now).stream()
                        .map(RolePermission::getPermissionId)
                        .collect(Collectors.toSet());

        var results = new ArrayList<PermissionAssignmentResult>();

        // Consistent lock ordering reduces deadlock risk.
        var permissionUuids = command.permissionUuids().stream().sorted().toList();

        for (var permissionUuid : permissionUuids) {
            var permission =
                    repository
                            .lockPermission(permissionUuid)
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Permission not found: " + permissionUuid));

            if (!permission.isAssignable()) {
                throw new IllegalStateException("Permission is inactive or deleted: " + permissionUuid);
            }

            if (assignedPermissionIds.contains(permission.getId())) {
                throw new DuplicateResourceException("Permission is already assigned: " + permission.getName());
            }

            var assignment =
                    RolePermission.assign(
                            role.getId(), permission.getId(), actor, now, command.expiresAt(), command.reason());

            var saved = repository.insert(assignment);

            results.add(PermissionAssignmentResult.from(saved, role.getUuid(), permission.getUuid()));

            assignedPermissionIds.add(permission.getId());
        }

        return List.copyOf(results);
    }
}
