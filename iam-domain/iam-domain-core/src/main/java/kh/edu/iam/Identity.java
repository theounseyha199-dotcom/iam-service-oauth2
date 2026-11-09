package kh.edu.iam;

import lombok.Getter;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class Identity {

    private final Long id;
    private final UUID uuid;
    private final Long userId;
    private final IdentityProvider provider;
    private final String providerSubject;
    private final Instant linkedAt;

    private String email;
    private boolean emailVerified;
    private String displayName;
    private String username;
    private String pictureUrl;

    // JSON document; mapped to JSONB by the persistence adapter.
    private String attributes;

    private Instant lastLoginAt;
    private AuditMetadata audit;
    private DeletionMetadata deletion;

    public Identity(
            Long id,
            UUID uuid,
            Long userId,
            IdentityProvider provider,
            String providerSubject,
            String email,
            boolean emailVerified,
            String displayName,
            String username,
            String pictureUrl,
            String attributes,
            Instant linkedAt,
            Instant lastLoginAt,
            AuditMetadata audit,
            DeletionMetadata deletion
    ) {
        this.id = id;
        this.uuid = Objects.requireNonNull(uuid, "uuid is required");
        this.userId = Objects.requireNonNull(
                userId, "userId is required"
        );

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "userId must be positive"
            );
        }

        this.provider = Objects.requireNonNull(
                provider, "provider is required"
        );
        this.providerSubject = required(
                providerSubject, 255, "providerSubject"
        );

        this.email = normalizeEmail(email);

        if (emailVerified && this.email == null) {
            throw new IllegalArgumentException(
                    "A verified email must have a value"
            );
        }

        this.emailVerified = emailVerified;
        this.displayName = optional(displayName, 150, "displayName");
        this.username = optional(username, 100, "username");
        this.pictureUrl = optional(pictureUrl, 500, "pictureUrl");
        this.attributes = attributes;
        this.linkedAt = Objects.requireNonNull(
                linkedAt, "linkedAt is required"
        );

        if (lastLoginAt != null && lastLoginAt.isBefore(linkedAt)) {
            throw new IllegalArgumentException(
                    "lastLoginAt cannot be before linkedAt"
            );
        }

        this.lastLoginAt = lastLoginAt;
        this.audit = Objects.requireNonNull(audit, "audit is required");
        this.deletion = Objects.requireNonNull(
                deletion, "deletion is required"
        );
    }

    public static Identity link(
            Long userId,
            IdentityProvider provider,
            String providerSubject,
            String email,
            boolean emailVerified,
            String displayName,
            String username,
            String pictureUrl,
            String attributes,
            String actor,
            Instant now
    ) {
        return new Identity(
                null,
                UUID.randomUUID(),
                userId,
                provider,
                providerSubject,
                email,
                emailVerified,
                displayName,
                username,
                pictureUrl,
                attributes,
                now,
                null,
                AuditMetadata.create(actor, now),
                DeletionMetadata.active()
        );
    }

    public void recordLogin(String actor, Instant now) {
        requireNotDeleted();

        Objects.requireNonNull(now, "now is required");

        if (now.isBefore(linkedAt)
                || (lastLoginAt != null && now.isBefore(lastLoginAt))) {
            throw new IllegalArgumentException(
                    "Login time cannot move backwards"
            );
        }

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.lastLoginAt = now;
        this.audit = updatedAudit;
    }

    public void unlink(String actor, Instant now) {
        requireNotDeleted();

        AuditMetadata updatedAudit = audit.updated(actor, now);
        DeletionMetadata deleted = DeletionMetadata.deleted(actor, now);

        this.deletion = deleted;
        this.audit = updatedAudit;
    }

    private void requireNotDeleted() {
        if (deletion.deleted()) {
            throw new IllegalStateException(
                    "Deleted identities cannot be changed"
            );
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalized = email.strip().toLowerCase(Locale.ROOT);
        return required(normalized, 255, "email");
    }

    private static String required(
            String value,
            int maxLength,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required"
            );
        }

        return optional(value, maxLength, field);
    }

    private static String optional(
            String value,
            int maxLength,
            String field
    ) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " cannot exceed "
                            + maxLength + " characters"
            );
        }

        return value;
    }
}
