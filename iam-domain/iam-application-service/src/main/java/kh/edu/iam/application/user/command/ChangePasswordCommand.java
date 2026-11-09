package kh.edu.iam.application.user.command;

import java.util.UUID;

public record ChangePasswordCommand(UUID userUuid, String currentPassword, String newPassword, String confirmPassword) {

    @Override
    public String toString() {
        return "ChangePasswordCommand[passwords=REDACTED]";
    }
}
