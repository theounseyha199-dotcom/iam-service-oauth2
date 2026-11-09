package kh.edu.iam.application.permission.command;

import java.util.Objects;
import java.util.UUID;

public record UpdatePermissionCommand(
        UUID permissionUuid,
        String description
) {
    public UpdatePermissionCommand {
        Objects.requireNonNull(permissionUuid, "permissionUuid is required");
    }
}