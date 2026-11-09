package kh.edu.iam.application.permission.usecase;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.permission.command.UpdatePermissionCommand;
import kh.edu.iam.application.permission.result.PermissionResult;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.application.port.out.TransactionRunner;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class UpdatePermissionUseCase {

    private final TransactionRunner transactions;

    private final PermissionRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public PermissionResult execute(UpdatePermissionCommand command) {
        return transactions.required(
                () -> {
                    Objects.requireNonNull(command, "command is required");

                    var permission =
                            repository
                                    .findByUuid(command.permissionUuid())
                                    .orElseThrow(
                                            () ->
                                                    new ResourceNotFoundException(
                                                            "Permission not found: " + command.permissionUuid()));

                    String previousName = permission.getName();
                    permission.update(
                            command.resource().orElse(permission.getResource()),
                            command.action().orElse(permission.getAction()),
                            command.description().orElse(permission.getDescription()),
                            actorProvider.currentActor(),
                            clock.instant());

                    if (!previousName.equals(permission.getName()) && repository.existsByName(permission.getName())) {
                        throw new kh.edu.iam.application.exception.DuplicateResourceException(
                                "Permission already exists");
                    }
                    return PermissionResult.from(repository.save(permission));
                });
    }
}
