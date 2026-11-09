package kh.edu.iam.application.user.usecase;

import kh.edu.iam.application.common.PasswordPolicy;
import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PasswordHasher;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.command.ChangePasswordCommand;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class ChangePasswordUseCase {
    private final UserRepository users;
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;
    private final CurrentActorProvider actors;
    private final Clock clock;
    private final PasswordHasher passwords;

    public void execute(ChangePasswordCommand command) {
        Objects.requireNonNull(command);
        PasswordPolicy.validate(command.newPassword(), command.confirmPassword());
        transactions.required(
                () -> {
                    var user = support.lock(command.userUuid());
                    if (!user.hasPassword()
                            || command.currentPassword() == null
                            || !passwords.matches(command.currentPassword(), user.getPasswordHash())) {
                        throw new ApplicationException("invalid_current_password", "Current password is incorrect");
                    }
                    user.replacePasswordHash(
                            passwords.hash(command.newPassword()), actors.currentActor(), clock.instant());
                    users.save(user);
                    return null;
                });
    }
}
