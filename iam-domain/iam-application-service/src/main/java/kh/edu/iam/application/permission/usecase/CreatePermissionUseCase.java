package kh.edu.iam.application.permission.usecase;

import kh.edu.iam.Permission;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.permission.command.CreatePermissionCommand;
import kh.edu.iam.application.permission.result.PermissionResult;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.application.port.out.TransactionRunner;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class CreatePermissionUseCase {

    private final TransactionRunner transactions;

    private final PermissionRepository permissionRepository;
    private final CurrentActorProvider currentActorProvider;
    private final Clock clock;

    public PermissionResult execute(CreatePermissionCommand command) {
        return transactions.required(
                () -> {
                    Objects.requireNonNull(command, "command is required");

                    // The domain factory validates the supplied fields.
                    Permission permission =
                            Permission.create(
                                    command.resource(),
                                    command.action(),
                                    command.description(),
                                    currentActorProvider.currentActor(),
                                    clock.instant());

                    if (permissionRepository.existsByName(permission.getName())) {
                        throw new DuplicateResourceException("Permission already exists: " + permission.getName());
                    }

                    Permission saved = permissionRepository.save(permission);

                    return PermissionResult.from(saved);
                });
    }
}
