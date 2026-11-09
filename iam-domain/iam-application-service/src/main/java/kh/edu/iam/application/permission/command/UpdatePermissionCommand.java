package kh.edu.iam.application.permission.command;

import kh.edu.iam.application.common.FieldUpdate;

import java.util.UUID;

public record UpdatePermissionCommand(
        UUID permissionUuid,
        FieldUpdate<String> resource,
        FieldUpdate<String> action,
        FieldUpdate<String> description) {
    public UpdatePermissionCommand(UUID uuid, String description) {
        this(uuid, FieldUpdate.unchanged(), FieldUpdate.unchanged(), FieldUpdate.set(description));
    }
}
