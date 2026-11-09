package kh.edu.iam.application.user.usecase;

import kh.edu.iam.UserPermission;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.user.command.GrantPermissionsToUserCommand;
import kh.edu.iam.application.user.result.PermissionGrantResult;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public final class GrantPermissionsToUserUseCase {
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;
    private final CurrentActorProvider actors;
    private final Clock clock;
    private final UserAssignmentRepository assignments;
    private final RolePermissionRepository targets;

    public List<PermissionGrantResult> execute(GrantPermissionsToUserCommand command) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(command.effect(), "effect is required");
        var ids = List.copyOf(Objects.requireNonNull(command.permissionUuids()));
        if (ids.isEmpty() || ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException("Supply distinct permission UUIDs");
        }
        return transactions.required(
                () -> {
                    var user = support.lock(command.userUuid());
                    var now = clock.instant();
                    var existing = assignments.findPermissions(user.getId());
                    var results = new ArrayList<PermissionGrantResult>();
                    for (var uuid : ids.stream().sorted().toList()) {
                        var p =
                                targets.lockPermission(uuid)
                                        .orElseThrow(
                                                () -> new ResourceNotFoundException("Permission not found: " + uuid));
                        if (!p.isAssignable()) {
                            throw new IllegalStateException("Permission is inactive");
                        }
                        if (existing.stream()
                                .anyMatch(
                                        a ->
                                                a.isActiveAt(now)
                                                        && a.getPermissionId().equals(p.getId())
                                                        && a.getEffect() == command.effect())) {
                            throw new DuplicateResourceException("Permission effect already assigned");
                        }
                        var a =
                                assignments.insertPermission(
                                        UserPermission.assign(
                                                user.getId(),
                                                p.getId(),
                                                command.effect(),
                                                actors.currentActor(),
                                                now,
                                                command.expiresAt(),
                                                command.reason()));
                        results.add(PermissionGrantResult.from(a, uuid, p.getName()));
                    }
                    return List.copyOf(results);
                });
    }
}
