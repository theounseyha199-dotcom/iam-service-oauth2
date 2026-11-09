package kh.edu.iam;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Role {

    private final Long id;
    private final UUID uuid;
    private final boolean system;

    private String name;
    private String description;
    private boolean active;
    private AuditMetadata audit;
    private DeletionMetadata deletion;

    public Role(
            Long id,
            UUID uuid,
            String name,
            String description,
            boolean system,
            boolean active,
            AuditMetadata audit,
            DeletionMetadata deletion
    ) {
        this.id = id;
        this.uuid = Objects.requireNonNull(uuid, "uuid is required");
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.system = system;
        this.active = active;
        this.audit = Objects.requireNonNull(audit, "audit is required");
        this.deletion = Objects.requireNonNull(
                deletion, "deletion is required"
        );
    }

    public static Role create(
            String name,
            String description,
            String actor,
            Instant now
    ) {
        return new Role(
                null,
                UUID.randomUUID(),
                name,
                description,
                false,
                true,
                AuditMetadata.create(actor, now),
                DeletionMetadata.active()
        );
    }

    public void update(
            String name,
            String description,
            String actor,
            Instant now
    ) {
        requireEditable();

        String validatedName = validateName(name);
        String validatedDescription = validateDescription(description);
        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.name = validatedName;
        this.description = validatedDescription;
        this.audit = updatedAudit;
    }

    public void activate(String actor, Instant now) {
        requireEditable();

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.active = true;
        this.audit = updatedAudit;
    }

    public void deactivate(String actor, Instant now) {
        requireEditable();

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.active = false;
        this.audit = updatedAudit;
    }

    public void delete(String actor, Instant now) {
        requireEditable();

        AuditMetadata updatedAudit = audit.updated(actor, now);
        DeletionMetadata deleted = DeletionMetadata.deleted(actor, now);

        this.active = false;
        this.deletion = deleted;
        this.audit = updatedAudit;
    }

    public boolean isAssignable() {
        return active && !deletion.deleted();
    }

    public String getAuthority() {
        return "ROLE_" + name;
    }

    private void requireEditable() {
        if (deletion.deleted()) {
            throw new IllegalStateException(
                    "Deleted roles cannot be changed"
            );
        }

        if (system) {
            throw new IllegalStateException(
                    "System roles cannot be changed or deleted"
            );
        }
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank() || name.length() > 50) {
            throw new IllegalArgumentException(
                    "Role name must contain 1 to 50 characters"
            );
        }

        if (!name.equals(name.strip())) {
            throw new IllegalArgumentException(
                    "Role name cannot contain surrounding whitespace"
            );
        }

        return name;
    }

    private static String validateDescription(String description) {
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 255 characters"
            );
        }

        return description;
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isActive() {
        return active;
    }

    public AuditMetadata getAudit() {
        return audit;
    }

    public DeletionMetadata getDeletion() {
        return deletion;
    }
}