package kh.edu.iam.application.user.usecase;

import kh.edu.iam.Role;
import kh.edu.iam.User;
import kh.edu.iam.UserProfile;
import kh.edu.iam.UserRole;
import kh.edu.iam.application.common.PasswordPolicy;
import kh.edu.iam.application.port.out.PasswordHasher;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.command.RegisterUserCommand;
import kh.edu.iam.application.user.result.UserResult;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class RegisterUserUseCase {
    private final UserRepository users;
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;
    private final PasswordHasher passwords;
    private final RoleRepository roles;
    private final UserAssignmentRepository assignments;
    private final Clock clock;

    public UserResult execute(RegisterUserCommand command) {
        Objects.requireNonNull(command);
        PasswordPolicy.validate(command.password(), command.confirmPassword());
        return transactions.required(
                () -> {
                    var now = clock.instant();
                    var user =
                            User.create(
                                    command.username(),
                                    command.email(),
                                    passwords.hash(command.password()),
                                    UserProfile.basic(command.givenName(), command.familyName(), command.locale()),
                                    "registration",
                                    now);
                    support.checkUnique(user);
                    var saved = users.save(user);
                    var role =
                            roles.findByName("USER")
                                    .filter(Role::isAssignable)
                                    .orElseThrow(() -> new IllegalStateException("Default USER role is unavailable"));
                    assignments.insertRole(
                            UserRole.assign(
                                    saved.getId(),
                                    role.getId(),
                                    "registration",
                                    now,
                                    null,
                                    "Default registration role"));
                    return support.result(saved);
                });
    }
}
