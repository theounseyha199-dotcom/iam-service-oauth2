package kh.edu.iam.application.identity.usecase;

import kh.edu.iam.Identity;
import kh.edu.iam.Role;
import kh.edu.iam.User;
import kh.edu.iam.UserProfile;
import kh.edu.iam.UserRole;
import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.identity.command.FederatedIdentityCommand;
import kh.edu.iam.application.identity.service.IdentityApplicationSupport;
import kh.edu.iam.application.port.out.IdentityRepository;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.result.AuthUserResult;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class FindOrProvisionFederatedUserUseCase {
    private final IdentityRepository identities;
    private final UserApplicationSupport users;
    private final TransactionRunner transactions;
    private final IdentityApplicationSupport support;
    private final UserRepository userRepository;
    private final RoleRepository roles;
    private final UserAssignmentRepository assignments;
    private final Clock clock;

    public AuthUserResult execute(FederatedIdentityCommand command) {
        Objects.requireNonNull(command);
        return transactions.required(
                () -> {
                    identities.lockProviderSubject(command.provider(), command.providerSubject());
                    var existing = identities.findByProviderSubject(command.provider(), command.providerSubject());
                    User user;
                    Identity identity;
                    var now = clock.instant();
                    if (existing.isPresent()) {
                        identity = existing.get();
                        if (identity.getDeletion().deleted()) {
                            throw new IllegalStateException("Identity was unlinked; explicitly link it again");
                        }
                        var owner =
                                userRepository
                                        .findById(identity.getUserId())
                                        .orElseThrow(
                                                () -> new ResourceNotFoundException("Identity owner is unavailable"));
                        user = users.lock(owner.getUuid());
                    } else {
                        if (command.email() != null && userRepository.existsByEmail(command.email())) {
                            throw new ApplicationException(
                                    "identity_link_required",
                                    "Sign in to the existing account and explicitly link this provider");
                        }
                        if (command.email() == null
                                || command.email().isBlank()
                                || command.givenName() == null
                                || command.givenName().isBlank()
                                || command.familyName() == null
                                || command.familyName().isBlank()) {
                            throw new ApplicationException(
                                    "profile_completion_required", "Complete the required account profile");
                        }
                        String id = UUID.randomUUID().toString().replace("-", "");
                        String email = command.email();
                        String given = command.givenName();
                        String family = command.familyName();
                        var provisioned =
                                User.create(
                                        "social_" + id,
                                        email,
                                        null,
                                        UserProfile.basic(
                                                given, family, command.locale() == null ? "en" : command.locale()),
                                        "federation",
                                        now);
                        if (command.emailVerified()) {
                            provisioned.verifyEmail("federation", now);
                        }
                        user = userRepository.save(provisioned);
                        var role =
                                roles.findByName("USER")
                                        .filter(Role::isAssignable)
                                        .orElseThrow(
                                                () -> new IllegalStateException("Default USER role is unavailable"));
                        assignments.insertRole(
                                UserRole.assign(
                                        user.getId(),
                                        role.getId(),
                                        "federation",
                                        now,
                                        null,
                                        "Default federated user role"));
                        identity = support.link(user, command, "federation");
                    }
                    if (!user.canAuthenticate()) {
                        throw new IllegalStateException("User cannot sign in");
                    }
                    identity.recordLogin("federation", clock.instant());
                    identities.save(identity);
                    user.recordLoginAttempt(true, "federation", clock.instant());
                    return users.auth(userRepository.save(user));
                });
    }
}
