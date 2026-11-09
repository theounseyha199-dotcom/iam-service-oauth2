package kh.edu.iam;

import java.time.Instant;
import java.util.Objects;

public record DeletionMetadata(boolean deleted, String deletedBy, Instant deletedAt) {
    public DeletionMetadata {
        if (deleted) {
            AuditMetadata.requireActor(deletedBy);
            Objects.requireNonNull(deletedAt, "deletedAt is required");
        } else if (deletedBy != null || deletedAt != null) {
            throw new IllegalArgumentException("Active records cannot have deletion metadata");
        }
    }

    public static DeletionMetadata active() {
        return new DeletionMetadata(false, null, null);
    }

    public static DeletionMetadata deleted(String actor, Instant now) {
        return new DeletionMetadata(true, actor, now);
    }
}
