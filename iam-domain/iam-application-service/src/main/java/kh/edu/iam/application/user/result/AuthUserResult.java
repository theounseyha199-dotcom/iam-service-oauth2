package kh.edu.iam.application.user.result;

import kh.edu.iam.User;
import kh.edu.iam.UserProfile;

import java.util.Set;
import java.util.UUID;

/**
 * Internal authentication result. Never return this as an HTTP response.
 */
public record AuthUserResult(
        UUID uuid,
        String username,
        String email,
        String passwordHash,
        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired,
        UserProfile profile,
        Set<String> authorities) {
    public AuthUserResult {
        authorities = Set.copyOf(authorities);
    }

    public static AuthUserResult from(User u, Set<String> authorities) {
        return new AuthUserResult(
                u.getUuid(),
                u.getUsername(),
                u.getEmail(),
                u.getPasswordHash(),
                u.isEnabled(),
                u.isAccountNonExpired(),
                u.isAccountNonLocked(),
                u.isCredentialsNonExpired(),
                u.getProfile(),
                authorities);
    }

    @Override
    public String toString() {
        return "AuthUserResult[uuid=" + uuid + ", username=" + username + "]";
    }
}
