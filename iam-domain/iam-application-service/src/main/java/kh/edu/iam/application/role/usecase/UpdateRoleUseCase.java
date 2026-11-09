package kh.edu.iam.application.role.usecase;

import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.role.command.UpdateRoleCommand;
import kh.edu.iam.application.role.result.RoleResult;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class UpdateRoleUseCase {

    private final TransactionRunner transactions;

    private final RoleRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public RoleResult execute(UpdateRoleCommand command) {
        return transactions.required(
                () -> {
                    Objects.requireNonNull(command, "command is required");

                    var role =
                            repository
                                    .findByUuid(command.roleUuid())
                                    .orElseThrow(
                                            () ->
                                                    new ResourceNotFoundException(
                                                            "Role not found: " + command.roleUuid()));

                    String previousName = role.getName();

                    // The domain validates the values and protects system roles.
                    role.update(
                            command.name().orElse(role.getName()),
                            command.description().orElse(role.getDescription()),
                            actorProvider.currentActor(),
                            clock.instant());

                    if (!previousName.equals(role.getName()) && repository.existsByName(role.getName())) {
                        throw new DuplicateResourceException("Role already exists: " + role.getName());
                    }

                    return RoleResult.from(repository.save(role));
                });
    }
}
