package kh.edu.iam.application.identity.command;

import kh.edu.iam.IdentityProvider;

import java.util.Objects;

public record FederatedIdentityCommand(
        IdentityProvider provider,
        String providerSubject,
        String email,
        boolean emailVerified,
        String displayName,
        String username,
        String pictureUrl,
        String attributes,
        String givenName,
        String familyName,
        String locale) {
    public FederatedIdentityCommand {
        Objects.requireNonNull(provider, "provider is required");
        if (providerSubject == null || providerSubject.isBlank() || providerSubject.length() > 255) {
            throw new IllegalArgumentException("Invalid provider subject");
        }
        if (emailVerified && (email == null || email.isBlank())) {
            throw new IllegalArgumentException("Verified email requires a value");
        }
    }
}
