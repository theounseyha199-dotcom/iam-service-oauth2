package kh.edu.iam.application.identity.usecase;

import kh.edu.iam.application.identity.result.IdentityResult;
import kh.edu.iam.application.port.out.IdentityRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class ListUserIdentitiesUseCase {
    private final IdentityRepository identities;
    private final UserApplicationSupport users;
    private final TransactionRunner transactions;

    public List<IdentityResult> execute(UUID userUuid) {
        return transactions.required(
                () ->
                        identities.findByUserId(users.require(userUuid).getId()).stream()
                                .map(IdentityResult::from)
                                .toList());
    }
}
