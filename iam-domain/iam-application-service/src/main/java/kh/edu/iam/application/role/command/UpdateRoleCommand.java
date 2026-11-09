package kh.edu.iam.application.role.command;

import kh.edu.iam.application.common.FieldUpdate;

import java.util.UUID;

public record UpdateRoleCommand(UUID roleUuid, FieldUpdate<String> name, FieldUpdate<String> description) {
    public UpdateRoleCommand(UUID uuid, String name, String description) {
        this(uuid, FieldUpdate.set(name), FieldUpdate.set(description));
    }
}
