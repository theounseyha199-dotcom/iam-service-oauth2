package kh.edu.iam.application.user.result;

import kh.edu.iam.User;
import kh.edu.iam.UserProfile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResult(
        UUID uuid,
        String username,
        String email,
        boolean emailVerified,
        String name,
        String locale,
        boolean enabled,
        boolean accountNonLocked,
        boolean accountNonExpired,
        boolean credentialsNonExpired,
        UserProfile profile,
        List<RoleAssignmentResult> roles,
        Instant createdAt,
        Instant updatedAt,
        long version) {
    public UserResult {
        roles = List.copyOf(roles);
    }

    public static UserResult from(User u, List<RoleAssignmentResult> roles) {
        return new UserResult(
                u.getUuid(),
                u.getUsername(),
                u.getEmail(),
                u.isEmailVerified(),
                u.getProfile().name(),
                u.getProfile().locale(),
                u.isEnabled(),
                u.isAccountNonLocked(),
                u.isAccountNonExpired(),
                u.isCredentialsNonExpired(),
                u.getProfile(),
                roles,
                u.getAudit().createdAt(),
                u.getAudit().updatedAt(),
                u.getAudit().version());
    }
}
