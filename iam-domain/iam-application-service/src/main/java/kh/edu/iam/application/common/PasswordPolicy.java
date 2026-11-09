package kh.edu.iam.application.common;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static void validate(String password, String confirmation) {
        Objects.requireNonNull(password, "password is required");
        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (password.isBlank() || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters and at most 72 UTF-8 bytes");
        }
    }
}
