package kh.edu.iam;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Permission {
    private final Long id;
    private final UUID uuid;
    private String resource;
    private String action;
    private final boolean system;

    private String description;
    private boolean active;
    private AuditMetadata audit;
    private DeletionMetadata deletion;

    public Permission(
            Long id,
            UUID uuid,
            String resource,
            String action,
            String description,
            boolean system,
            boolean active,
            AuditMetadata audit,
            DeletionMetadata deletion) {
        this.id = id;
        this.uuid = Objects.requireNonNull(uuid, "uuid is required");
        this.resource = validatePart(resource, "resource");
        this.action = validatePart(action, "action");

        if ((this.resource + ":" + this.action).length() > 100) {
            throw new IllegalArgumentException("Permission name cannot exceed 100 characters");
        }

        this.description = validateDescription(description);
        this.system = system;
        this.active = active;
        this.audit = Objects.requireNonNull(audit, "audit is required");
        this.deletion = Objects.requireNonNull(deletion, "deletion is required");
    }

    public static Permission create(String resource, String action, String description, String actor, Instant now) {
        return new Permission(
                null,
                UUID.randomUUID(),
                resource,
                action,
                description,
                false,
                true,
                AuditMetadata.create(actor, now),
                DeletionMetadata.active());
    }

    public void update(String resource, String action, String description, String actor, Instant now) {
        requireEditable();
        String checkedResource = validatePart(resource, "resource");
        String checkedAction = validatePart(action, "action");
        if ((checkedResource + ":" + checkedAction).length() > 100) {
            throw new IllegalArgumentException("Permission name cannot exceed 100 characters");
        }
        String checkedDescription = validateDescription(description);
        AuditMetadata changed = audit.updated(actor, now);
        this.resource = checkedResource;
        this.action = checkedAction;
        this.description = checkedDescription;
        this.audit = changed;
    }

    public void updateDescription(String description, String actor, Instant now) {
        requireEditable();

        String validated = validateDescription(description);
        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.description = validated;
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

    private void requireEditable() {
        if (deletion.deleted()) {
            throw new IllegalStateException("Deleted permissions cannot be changed");
        }

        if (system) {
            throw new IllegalStateException("System permissions cannot be changed or deleted");
        }
    }

    private static String validatePart(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }

        if (value.length() > 50) {
            throw new IllegalArgumentException(field + " cannot exceed 50 characters");
        }

        if (!value.equals(value.strip()) || value.contains(":")) {
            throw new IllegalArgumentException(field + " cannot contain ':' or surrounding whitespace");
        }

        return value;
    }

    private static String validateDescription(String description) {
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters");
        }

        return description;
    }

    public String getName() {
        return resource + ":" + action;
    }

    public void activate(String actor, Instant now) {
        requireEditable();

        AuditMetadata updatedAudit = audit.updated(actor, now);

        this.active = true;
        this.audit = updatedAudit;
    }
}
