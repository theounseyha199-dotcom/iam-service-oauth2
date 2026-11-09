package kh.edu.iam.application.user.usecase;

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

    public AuthUserResult execute(String usernameOrEmail) {
        return transactions.required(
                () -> {
                    var user =
                            users.findByUsernameOrEmail(Objects.requireNonNull(usernameOrEmail))
                                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return support.auth(user);
                });
    }
}
