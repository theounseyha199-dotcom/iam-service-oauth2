package kh.edu.iam;

import java.time.Instant;
import java.util.Objects;

public record AuditMetadata(
        Instant createdAt, String createdBy,
        Instant updatedAt, String updatedBy, long version
) {
    public AuditMetadata {
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
        requireActor(createdBy);
        requireActor(updatedBy);
        if (updatedAt.isBefore(createdAt) || version < 0) {
            throw new IllegalArgumentException("Invalid audit time or version");
        }
    }

    public static AuditMetadata create(String actor, Instant now) {
        return new AuditMetadata(now, actor, now, actor, 0);
    }

    public AuditMetadata updated(String actor, Instant now) {
        Objects.requireNonNull(now, "now is required");
        if (now.isBefore(updatedAt)) {
            throw new IllegalArgumentException("Audit time cannot move backwards");
        }
        return new AuditMetadata(createdAt, createdBy, now, actor, version);
    }

    static void requireActor(String actor) {
        if (actor == null || actor.isBlank() || actor.length() > 100) {
            throw new IllegalArgumentException("Actor must contain 1 to 100 characters");
        }
    }
}
