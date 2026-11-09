package kh.edu.iam.application.role.command;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreateRoleCommand(String name, String description, List<UUID> permissionUuids) {
    public CreateRoleCommand {
        Objects.requireNonNull(name, "name is required");

        permissionUuids = permissionUuids == null ? List.of() : List.copyOf(permissionUuids);

        if (permissionUuids.stream().distinct().count() != permissionUuids.size()) {
            throw new IllegalArgumentException("permissionUuids must not contain duplicates");
        }
    }
}
