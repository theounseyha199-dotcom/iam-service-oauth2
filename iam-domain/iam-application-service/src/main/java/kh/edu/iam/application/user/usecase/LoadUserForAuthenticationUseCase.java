package kh.edu.iam.application.user.usecase;

import kh.edu.iam.application.port.out.EmailVerification;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.result.AuthUserResult;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public final class LoadUserForAuthenticationUseCase {
    private final UserRepository users;
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;
    private final EmailVerification verification;

    public AuthUserResult execute(String usernameOrEmail) {
        return transactions.required(
                () -> {
                    var user =
                            users.findByUsernameOrEmail(Objects.requireNonNull(usernameOrEmail))
                                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    var auth = support.auth(user);
                    if (!verification.isPending(user.getUuid())) return auth;
                    return new AuthUserResult(auth.uuid(), auth.username(), auth.email(), auth.passwordHash(),
                            false, auth.accountNonExpired(), auth.accountNonLocked(), auth.credentialsNonExpired(),
                            auth.profile(), auth.authorities());
                });
    }
}
