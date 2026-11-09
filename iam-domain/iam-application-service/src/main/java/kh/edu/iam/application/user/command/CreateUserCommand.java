package kh.edu.iam.application.user.command;

import kh.edu.iam.UserProfile;

import java.util.List;
import java.util.UUID;

public record CreateUserCommand(
        String username, String email, String password, UserProfile profile, List<UUID> roleUuids) {
    public CreateUserCommand {
        roleUuids = roleUuids == null ? List.of() : List.copyOf(roleUuids);
    }

    @Override
    public String toString() {
        return "CreateUserCommand[passwords=REDACTED]";
    }
}
