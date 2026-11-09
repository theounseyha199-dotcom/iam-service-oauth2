package kh.edu.iam.application.user.usecase;

import kh.edu.iam.UserRole;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.user.command.AssignRolesToUserCommand;
import kh.edu.iam.application.user.result.RoleAssignmentResult;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class AssignRolesToUserUseCase {
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;
    private final CurrentActorProvider actors;
    private final Clock clock;
    private final UserAssignmentRepository assignments;
    private final RolePermissionRepository targets;

    public List<RoleAssignmentResult> execute(AssignRolesToUserCommand command) {
        Objects.requireNonNull(command);
        var ids = distinctIds(command.roleUuids());
        return transactions.required(
                () -> {
                    var user = support.lock(command.userUuid());
                    var now = clock.instant();
                    var active =
                            assignments.findRoles(user.getId()).stream()
                                    .filter(a -> a.isActiveAt(now))
                                    .map(UserRole::getRoleId)
                                    .collect(java.util.stream.Collectors.toSet());
                    var results = new ArrayList<RoleAssignmentResult>();
                    for (var uuid : ids) {
                        var role =
                                targets.lockRole(uuid)
                                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + uuid));
                        if (!role.isAssignable()) {
                            throw new IllegalStateException("Role is inactive");
                        }
                        if (!active.add(role.getId())) {
                            throw new DuplicateResourceException("Role already assigned");
                        }
                        var a =
                                assignments.insertRole(
                                        UserRole.assign(
                                                user.getId(),
                                                role.getId(),
                                                actors.currentActor(),
                                                now,
                                                command.expiresAt(),
                                                command.reason()));
                        results.add(RoleAssignmentResult.from(a, uuid, role.getName()));
                    }
                    return List.copyOf(results);
                });
    }

    private static List<UUID> distinctIds(List<UUID> ids) {
        var copy = List.copyOf(Objects.requireNonNull(ids));
        if (copy.isEmpty() || copy.stream().distinct().count() != copy.size()) {
            throw new IllegalArgumentException("Supply a non-empty list of distinct UUIDs");
        }
        return copy.stream().sorted().toList();
    }
}
