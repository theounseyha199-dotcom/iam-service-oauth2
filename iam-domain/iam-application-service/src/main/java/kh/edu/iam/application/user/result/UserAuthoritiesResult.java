package kh.edu.iam.application.user.result;

import java.util.List;
import java.util.Set;

public record UserAuthoritiesResult(
        List<RoleAssignmentResult> roles,
        List<PermissionGrantResult> directPermissions,
        Set<String> effectivePermissions,
        Set<String> authorities) {
    public UserAuthoritiesResult {
        roles = List.copyOf(roles);
        directPermissions = List.copyOf(directPermissions);
        effectivePermissions = Set.copyOf(effectivePermissions);
        authorities = Set.copyOf(authorities);
    }
}
