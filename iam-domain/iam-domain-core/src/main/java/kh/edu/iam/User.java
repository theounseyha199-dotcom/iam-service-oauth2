package kh.edu.iam;

import lombok.Getter;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class User {

    private final Long id;
    private final UUID uuid;
    private String username;

    private String email;
    private boolean emailVerified;
    private String passwordHash;
    private UserProfile profile;

    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;

    private int failedLoginAttempts;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;

    private AuditMetadata audit;
    private DeletionMetadata deletion;

    public User(
            Long id,
            UUID uuid,
            String username,
            String email,
            boolean emailVerified,
            String passwordHash,
            UserProfile profile,
            boolean enabled,
            boolean accountNonExpired,
            boolean accountNonLocked,
            boolean credentialsNonExpired,
            int failedLoginAttempts,
            Instant lastLoginAt,
            Instant passwordChangedAt,
            AuditMetadata audit,
            DeletionMetadata deletion) {
        this.id = id;
        this.uuid = Objects.requireNonNull(uuid, "uuid is required");
        this.username = validateUsername(username);
        this.email = validateEmail(email);
        this.emailVerified = emailVerified;
        this.passwordHash = validatePasswordHash(passwordHash);
        this.profile = Objects.requireNonNull(profile, "profile is required");

        if (failedLoginAttempts < 0) {
            throw new IllegalArgumentException("failedLoginAttempts cannot be negative");
        }

        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lastLoginAt = lastLoginAt;
        this.passwordChangedAt = passwordChangedAt;
        this.audit = Objects.requireNonNull(audit, "audit is required");
        this.deletion = Objects.requireNonNull(deletion, "deletion is required");
    }

    public static User create(
            String username, String email, String passwordHash, UserProfile profile, String actor, Instant now) {
        return new User(
                null,
                UUID.randomUUID(),
                username,
                email,
                false,
                passwordHash,
                profile,
                true,
                true,
                true,
                true,
                0,
                null,
                passwordHash == null ? null : now,
                AuditMetadata.create(actor, now),
                DeletionMetadata.active());
    }

    public void updateProfile(UserProfile profile, String actor, Instant now) {
        requireNotDeleted();

        UserProfile validated = Objects.requireNonNull(profile, "profile is required");
        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.profile = validated;
        this.audit = updatedAudit;
    }

    public void changeUsername(String username, String actor, Instant now) {
        requireNotDeleted();
        String validated = validateUsername(username);
        if (validated.contains("@")) throw new IllegalArgumentException("Username cannot contain @");
        this.username = validated;
        this.audit = audit.updated(actor, now);
    }

    public void changeEmail(String email, String actor, Instant now) {
        requireNotDeleted();

        String validated = validateEmail(email);
        AuditMetadata updatedAudit = audit.updated(actor, now);

        if (!this.email.equals(validated)) {
            this.email = validated;
            this.emailVerified = false;
        }

        this.audit = updatedAudit;
    }

    public void verifyEmail(String actor, Instant now) {
        requireNotDeleted();
        AuditMetadata updatedAudit = audit.updated(actor, now);
        this.emailVerified = true;
        this.audit = updatedAudit;
    }

    public void replacePasswordHash(String passwordHash, String actor, Instant now) {
        requireNotDeleted();

        String validated = validatePasswordHash(passwordHash);

        if (validated == null) {
            throw new IllegalArgumentException("A new password hash is required");
        }

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.passwordHash = validated;
        this.passwordChangedAt = now;
        this.credentialsNonExpired = true;
        this.audit = updatedAudit;
    }

    public void enable(String actor, Instant now) {
        requireNotDeleted();

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.enabled = true;
        this.audit = updatedAudit;
    }

    public void disable(String actor, Instant now) {
        requireNotDeleted();

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.enabled = false;
        this.audit = updatedAudit;
    }

    public void lock(String actor, Instant now) {
        requireNotDeleted();

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.accountNonLocked = false;
        this.audit = updatedAudit;
    }

    public void unlock(String actor, Instant now) {
        requireNotDeleted();

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.accountNonLocked = true;
        this.failedLoginAttempts = 0;
        this.audit = updatedAudit;
    }

    public void recordLoginAttempt(boolean success, String actor, Instant now) {
        requireNotDeleted();

        if (success && !canAuthenticate()) {
            throw new IllegalStateException("User is not allowed to sign in");
        }

        int updatedAttempts = success ? 0 : Math.addExact(failedLoginAttempts, 1);

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.failedLoginAttempts = updatedAttempts;

        if (success) {
            this.lastLoginAt = now;
        }

        this.audit = updatedAudit;
    }

    public void delete(String actor, Instant now) {
        requireNotDeleted();

        AuditMetadata updatedAudit = audit.updated(actor, now);
        DeletionMetadata deleted = DeletionMetadata.deleted(actor, now);

        this.enabled = false;
        this.deletion = deleted;
        this.audit = updatedAudit;
    }

    public boolean canAuthenticate() {
        return !deletion.deleted() && enabled && accountNonExpired && accountNonLocked && credentialsNonExpired;
    }

    public boolean hasPassword() {
        return passwordHash != null;
    }

    private void requireNotDeleted() {
        if (deletion.deleted()) {
            throw new IllegalStateException("Deleted users cannot be changed");
        }
    }

    private static String validateUsername(String username) {
        if (username == null || username.isBlank() || username.length() > 50 || !username.equals(username.strip())) {
            throw new IllegalArgumentException(
                    "Username must contain 1 to 50 characters " + "without surrounding whitespace");
        }

        return username;
    }

    private static String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalized = email.strip().toLowerCase(Locale.ROOT);
        int separator = normalized.indexOf('@');

        if (normalized.length() > 255
                || separator <= 0
                || separator != normalized.lastIndexOf('@')
                || separator == normalized.length() - 1
                || normalized.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Invalid email");
        }

        return normalized;
    }

    private static String validatePasswordHash(String hash) {
        if (hash != null && (hash.isBlank() || hash.length() > 255)) {
            throw new IllegalArgumentException("Password hash must contain 1 to 255 characters");
        }

        return hash;
    }
}
