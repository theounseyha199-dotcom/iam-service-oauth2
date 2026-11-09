package kh.edu.iam.application.user.usecase;

import kh.edu.iam.User;
import kh.edu.iam.application.common.PasswordPolicy;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PasswordHasher;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.command.AssignRolesToUserCommand;
import kh.edu.iam.application.user.command.CreateUserCommand;
import kh.edu.iam.application.user.result.UserResult;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class CreateUserUseCase {
    private final UserRepository users;
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;
    private final CurrentActorProvider actors;
    private final Clock clock;
    private final PasswordHasher passwords;
    private final AssignRolesToUserUseCase assignRoles;

    public UserResult execute(CreateUserCommand command) {
        Objects.requireNonNull(command);
        PasswordPolicy.validate(command.password(), command.password());
        return transactions.required(
                () -> {
                    var user =
                            User.create(
                                    command.username(),
                                    command.email(),
                                    passwords.hash(command.password()),
                                    command.profile(),
                                    actors.currentActor(),
                                    clock.instant());
                    support.checkUnique(user);
                    var saved = users.save(user);
                    if (!command.roleUuids().isEmpty()) {
                        assignRoles.execute(
                                new AssignRolesToUserCommand(
                                        saved.getUuid(), command.roleUuids(), null, "Initial user roles"));
                    }
                    return support.result(saved);
                });
    }
}
