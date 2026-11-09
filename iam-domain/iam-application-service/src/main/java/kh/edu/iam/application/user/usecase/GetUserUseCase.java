package kh.edu.iam.application.user.usecase;

import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.user.result.UserResult;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public final class GetUserUseCase {
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;

    public UserResult execute(UUID userUuid) {
        return transactions.required(() -> support.result(support.require(userUuid)));
    }
}
