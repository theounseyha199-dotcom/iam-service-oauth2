package kh.edu.iam.application.user.result;

import kh.edu.iam.User;

import java.util.UUID;

public record UserSummary(UUID uuid, String username, String email, String name, boolean enabled) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getUuid(), user.getUsername(), user.getEmail(), user.getProfile().name(), user.isEnabled());
    }
}
