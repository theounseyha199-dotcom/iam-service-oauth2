package kh.edu.iam;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public abstract class Assignment {

    private final Long id;
    private final Instant assignedAt;
    private final String assignedBy;
    private final Instant expiresAt;
    private final String reason;

    private Instant revokedAt;
    private String revokedBy;

    protected Assignment(
            Long id,
            Instant assignedAt,
            String assignedBy,
            Instant expiresAt,
            Instant revokedAt,
            String revokedBy,
            String reason
    ) {
        this.id = id;
        this.assignedAt = Objects.requireNonNull(
                assignedAt, "assignedAt is required"
        );

        AuditMetadata.requireActor(assignedBy);

        if (expiresAt != null && !expiresAt.isAfter(assignedAt)) {
            throw new IllegalArgumentException(
                    "expiresAt must be after assignedAt"
            );
        }

        if ((revokedAt == null) != (revokedBy == null)) {
            throw new IllegalArgumentException(
                    "revokedAt and revokedBy must be provided together"
            );
        }

        if (revokedAt != null) {
            AuditMetadata.requireActor(revokedBy);

            if (revokedAt.isBefore(assignedAt)) {
                throw new IllegalArgumentException(
                        "revokedAt cannot be before assignedAt"
                );
            }
        }

        validateReason(reason);

        this.assignedBy = assignedBy;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.revokedBy = revokedBy;
        this.reason = reason;
    }

    public boolean isActiveAt(Instant now) {
        Objects.requireNonNull(now, "now is required");

        return !now.isBefore(assignedAt)
                && (revokedAt == null || now.isBefore(revokedAt))
                && (expiresAt == null || now.isBefore(expiresAt));
    }

    public void revoke(String actor, Instant now) {
        if (revokedAt != null) {
            throw new IllegalStateException(
                    "Assignment has already been revoked"
            );
        }

        AuditMetadata.requireActor(actor);
        Objects.requireNonNull(now, "now is required");

        if (now.isBefore(assignedAt)) {
            throw new IllegalArgumentException(
                    "Revocation time cannot be before assignment time"
            );
        }

        this.revokedAt = now;
        this.revokedBy = actor;
    }

    protected static Long requireId(Long id, String field) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    field + " must be positive"
            );
        }

        return id;
    }

    private static void validateReason(String reason) {
        if (reason != null && reason.length() > 255) {
            throw new IllegalArgumentException(
                    "reason cannot exceed 255 characters"
            );
        }
    }
}