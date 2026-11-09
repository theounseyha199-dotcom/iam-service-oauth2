package kh.edu.iam.application.identity.usecase;

import kh.edu.iam.application.identity.command.FederatedIdentityCommand;
import kh.edu.iam.application.identity.result.IdentityResult;
import kh.edu.iam.application.identity.service.IdentityApplicationSupport;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.IdentityRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class LinkIdentityUseCase {
    private final IdentityRepository identities;
    private final UserApplicationSupport users;
    private final TransactionRunner transactions;
    private final IdentityApplicationSupport support;
    private final CurrentActorProvider actors;

    public IdentityResult execute(UUID userUuid, FederatedIdentityCommand command) {
        Objects.requireNonNull(command);
        return transactions.required(
                () -> {
                    identities.lockProviderSubject(command.provider(), command.providerSubject());
                    var user = users.lock(userUuid);
                    return IdentityResult.from(support.link(user, command, actors.currentActor()));
                });
    }
}
