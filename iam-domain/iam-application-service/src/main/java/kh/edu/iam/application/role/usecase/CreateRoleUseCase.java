package kh.edu.iam.application.role.usecase;

import kh.edu.iam.Role;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.role.command.AssignPermissionsToRoleCommand;
import kh.edu.iam.application.role.command.CreateRoleCommand;
import kh.edu.iam.application.role.result.RoleResult;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class CreateRoleUseCase {

    private final RoleRepository roleRepository;
    private final AssignPermissionsToRoleUseCase assignPermissions;
    private final CurrentActorProvider actorProvider;
    private final TransactionRunner transactions;
    private final Clock clock;

    public RoleResult execute(CreateRoleCommand command) {
        Objects.requireNonNull(command, "command is required");

        return transactions.required(() -> create(command));
    }

    private RoleResult create(CreateRoleCommand command) {
        var role = Role.create(
                command.name(),
                command.description(),
                actorProvider.currentActor(),
                clock.instant()
        );

        if (roleRepository.existsByName(role.getName())) {
            throw new DuplicateResourceException(
                    "Role already exists: " + role.getName()
            );
        }

        var savedRole = roleRepository.save(role);

        if (!command.permissionUuids().isEmpty()) {
            assignPermissions.execute(
                    new AssignPermissionsToRoleCommand(
                            savedRole.getUuid(),
                            command.permissionUuids(),
                            null,
                            "Initial permissions at role creation"
                    )
            );
        }

        return RoleResult.from(savedRole);
    }
}