package kh.edu.iam.application.user.usecase;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@RequiredArgsConstructor
public final class RevokeRoleFromUserUseCase {
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;
    private final CurrentActorProvider actors;
    private final Clock clock;
    private final UserAssignmentRepository assignments;
    private final RoleRepository targetRepository;

    public void execute(UUID userUuid, UUID roleUuid, String reason) {
        if (reason != null && reason.length() > 255) {
            throw new IllegalArgumentException("Reason is too long");
        }
        transactions.required(
                () -> {
                    var user = support.lock(userUuid);
                    var now = clock.instant();
                    var matches =
                            assignments.findRoles(user.getId()).stream()
                                    .filter(a -> a.isActiveAt(now))
                                    .filter(
                                            a ->
                                                    targetRepository
                                                            .findById(a.getRoleId())
                                                            .map(t -> t.getUuid().equals(roleUuid))
                                                            .orElse(false))
                                    .toList();
                    if (matches.isEmpty()) {
                        throw new ResourceNotFoundException("Active assignment not found");
                    }
                    for (var a : matches) {
                        a.revoke(actors.currentActor(), now);
                        assignments.saveRoleRevocation(a, reason);
                    }
                    return null;
                });
    }
}
