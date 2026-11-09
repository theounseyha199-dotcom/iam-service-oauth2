package kh.edu.iam.application.identity.service;

import kh.edu.iam.Identity;
import kh.edu.iam.User;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.identity.command.FederatedIdentityCommand;
import kh.edu.iam.application.port.out.IdentityRepository;

import lombok.RequiredArgsConstructor;

import java.time.Clock;

@RequiredArgsConstructor
public final class IdentityApplicationSupport {
    private final IdentityRepository identities;
    private final Clock clock;

    public Identity link(User user, FederatedIdentityCommand command, String actor) {
        var existing = identities.findByProviderSubject(command.provider(), command.providerSubject());
        if (existing.isPresent()) {
            var i = existing.get();
            if (!i.getUserId().equals(user.getId())) {
                throw new DuplicateResourceException("Identity belongs to another user");
            }
            if (!i.getDeletion().deleted()) {
                throw new DuplicateResourceException("Identity is already linked");
            }
            if (identities.findByUserId(user.getId()).stream()
                    .anyMatch(other -> other.getProvider() == command.provider())) {
                throw new DuplicateResourceException("Provider is already linked");
            }
            i.relink(actor, clock.instant());
            return identities.save(i);
        }
        if (identities.findByUserId(user.getId()).stream().anyMatch(i -> i.getProvider() == command.provider())) {
            throw new DuplicateResourceException("Provider is already linked");
        }
        return identities.save(
                Identity.link(
                        user.getId(),
                        command.provider(),
                        command.providerSubject(),
                        command.email(),
                        command.emailVerified(),
                        command.displayName(),
                        command.username(),
                        command.pictureUrl(),
                        command.attributes(),
                        actor,
                        clock.instant()));
    }
}
