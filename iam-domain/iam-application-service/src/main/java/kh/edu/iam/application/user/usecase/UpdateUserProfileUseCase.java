package kh.edu.iam.application.user.usecase;

import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.command.UpdateUserProfileCommand;
import kh.edu.iam.application.user.result.UserResult;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class UpdateUserProfileUseCase {
    private final UserRepository users;
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;
    private final CurrentActorProvider actors;
    private final Clock clock;

    public UserResult execute(UpdateUserProfileCommand command) {
        Objects.requireNonNull(command);
        return transactions.required(
                () -> {
                    var user = support.lock(command.userUuid());
                    if (command.expectedVersion() != null && command.expectedVersion() != user.getAudit().version()) {
                        throw new ApplicationException(
                                "concurrent_update", "This profile was changed in another session");
                    }
                    var patch = Objects.requireNonNull(command.patch());
                    if (patch.email().supplied()
                            && !Objects.equals(user.getEmail(), patch.email().value())
                            && users.existsByEmail(Objects.requireNonNull(patch.email().value()))) {
                        throw new DuplicateResourceException("Email already exists", "email");
                    }
                    if (patch.username().supplied() && !Objects.equals(user.getUsername(), patch.username().value())
                            && users.existsByUsername(Objects.requireNonNull(patch.username().value()))) {
                        throw new DuplicateResourceException("Username already exists", "username");
                    }
                    patch.apply(user, actors.currentActor(), clock.instant());
                    return support.result(users.save(user));
                });
    }

    private static <T> T keep(T proposed, T current) {
        return proposed == null ? current : proposed;
    }
}
