package kh.edu.iam.application.role.command;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record AssignPermissionsToRoleCommand(
        UUID roleUuid,
        List<UUID> permissionUuids,
        Instant expiresAt,
        String reason
) {
    public AssignPermissionsToRoleCommand {
        Objects.requireNonNull(roleUuid, "roleUuid is required");

        permissionUuids = List.copyOf(
                Objects.requireNonNull(
                        permissionUuids,
                        "permissionUuids is required"
                )
        );

        if (permissionUuids.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one permission is required"
            );
        }

        if (permissionUuids.stream().distinct().count()
                != permissionUuids.size()) {
            throw new IllegalArgumentException(
                    "permissionUuids must not contain duplicates"
            );
        }

        if (reason != null && reason.length() > 255) {
            throw new IllegalArgumentException(
                    "reason cannot exceed 255 characters"
            );
        }
    }
}
