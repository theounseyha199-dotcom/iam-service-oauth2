package kh.edu.iam;

import java.util.Set;

public record UserAuthorities(
        Set<String> roles,
        Set<String> permissions
) {

    public UserAuthorities {
        roles = Set.copyOf(roles);
        permissions = Set.copyOf(permissions);
    }

    public Set<String> all() {
        var authorities = new java.util.HashSet<>(roles);
        authorities.addAll(permissions);

        return Set.copyOf(authorities);
    }
}