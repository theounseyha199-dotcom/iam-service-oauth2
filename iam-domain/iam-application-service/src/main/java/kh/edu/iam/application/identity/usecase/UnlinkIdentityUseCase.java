package kh.edu.iam.application.identity.usecase;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.IdentityRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class UnlinkIdentityUseCase {
    private final IdentityRepository identities;
    private final UserApplicationSupport users;
    private final TransactionRunner transactions;
    private final CurrentActorProvider actors;
    private final Clock clock;

    public void execute(UUID userUuid, UUID identityUuid) {
        transactions.required(
                () -> {
                    var user = users.lock(userUuid);
                    var identity =
                            identities
                                    .findByUuid(Objects.requireNonNull(identityUuid))
                                    .filter(i -> i.getUserId().equals(user.getId()))
                                    .orElseThrow(() -> new ResourceNotFoundException("Identity not found"));
                    if (!user.hasPassword() && identities.findByUserId(user.getId()).size() <= 1) {
                        throw new IllegalStateException("Cannot unlink the last sign-in method");
                    }
                    identity.unlink(actors.currentActor(), clock.instant());
                    identities.save(identity);
                    return null;
                });
    }
}
