package kh.edu.iam.application.user.usecase;

import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.user.result.UserAuthoritiesResult;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public final class GetUserAuthoritiesUseCase {
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;

    public UserAuthoritiesResult execute(UUID userUuid) {
        return transactions.required(() -> support.authorities(support.require(userUuid)));
    }
}
