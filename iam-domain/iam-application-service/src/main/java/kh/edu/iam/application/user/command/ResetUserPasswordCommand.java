package kh.edu.iam.application.user.command;

import java.util.UUID;

public record ResetUserPasswordCommand(UUID userUuid, String newPassword, String confirmPassword) {

    @Override
    public String toString() {
        return "ResetUserPasswordCommand[passwords=REDACTED]";
    }
}
