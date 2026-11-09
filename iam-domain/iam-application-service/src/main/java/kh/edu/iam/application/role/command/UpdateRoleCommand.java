package kh.edu.iam.application.role.command;

import java.util.Objects;
import java.util.UUID;

public record UpdateRoleCommand(
        UUID roleUuid,
        String name,
        String description
) {
    public UpdateRoleCommand {
        Objects.requireNonNull(roleUuid, "roleUuid is required");
    }
}
