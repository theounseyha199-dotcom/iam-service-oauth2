package kh.edu.iam.application.role.usecase;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.application.port.out.TransactionRunner;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class RevokePermissionFromRoleUseCase {

    private final RolePermissionRepository repository;
    private final CurrentActorProvider actorProvider;
    private final TransactionRunner transactions;
    private final Clock clock;

    public void execute(UUID roleUuid, UUID permissionUuid, String reason) {
        Objects.requireNonNull(roleUuid, "roleUuid is required");
        Objects.requireNonNull(permissionUuid, "permissionUuid is required");

        if (reason != null && reason.length() > 255) {
            throw new IllegalArgumentException("reason cannot exceed 255 characters");
        }

        transactions.required(
                () -> {
                    var role =
                            repository
                                    .lockRole(roleUuid)
                                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleUuid));

                    if (role.isSystem()) {
                        throw new IllegalStateException("System role permissions cannot be changed");
                    }

                    var now = clock.instant();

                    var assignment =
                            repository
                                    .findUnrevokedForUpdate(role.getId(), permissionUuid, now)
                                    .orElseThrow(
                                            () ->
                                                    new ResourceNotFoundException(
                                                            "No active permission assignment was found"));

                    assignment.revoke(actorProvider.currentActor(), now);

                    repository.saveRevocation(assignment, reason);

                    return null;
                });
    }
}
