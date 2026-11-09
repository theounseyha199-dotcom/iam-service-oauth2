package kh.edu.iam.application.role.result;

import kh.edu.iam.Role;

import java.util.UUID;

public record RoleSummary(UUID uuid, String name, String description, boolean system, boolean active) {

    public static RoleSummary from(Role role) {
        return new RoleSummary(role.getUuid(), role.getName(), role.getDescription(), role.isSystem(), role.isActive());
    }
}
