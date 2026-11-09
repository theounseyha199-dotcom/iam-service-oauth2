package kh.edu.iam.application.identity.result;

import kh.edu.iam.Identity;
import kh.edu.iam.IdentityProvider;

import java.time.Instant;
import java.util.UUID;

public record IdentityResult(
        UUID uuid,
        IdentityProvider provider,
        String email,
        boolean emailVerified,
        String displayName,
        String username,
        String pictureUrl,
        Instant linkedAt,
        Instant lastLoginAt) {
    public static IdentityResult from(Identity i) {
        return new IdentityResult(
                i.getUuid(),
                i.getProvider(),
                i.getEmail(),
                i.isEmailVerified(),
                i.getDisplayName(),
                i.getUsername(),
                i.getPictureUrl(),
                i.getLinkedAt(),
                i.getLastLoginAt());
    }
}
