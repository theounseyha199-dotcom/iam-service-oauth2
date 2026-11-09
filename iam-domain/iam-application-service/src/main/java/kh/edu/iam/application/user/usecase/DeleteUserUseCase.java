package kh.edu.iam.application.user.usecase;

import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@RequiredArgsConstructor
public final class DeleteUserUseCase {
    private final UserRepository users;
    private final UserApplicationSupport support;
    private final TransactionRunner transactions;
    private final CurrentActorProvider actors;
    private final Clock clock;

    public void execute(UUID userUuid) {
        transactions.required(
                () -> {
                    var user = support.lock(userUuid);
                    user.delete(actors.currentActor(), clock.instant());
                    users.save(user);
                    return null;
                });
    }
}
