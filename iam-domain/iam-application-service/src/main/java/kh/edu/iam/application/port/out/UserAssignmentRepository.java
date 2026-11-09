package kh.edu.iam.application.port.out;

import kh.edu.iam.UserPermission;
import kh.edu.iam.UserRole;

import java.util.List;

public interface UserAssignmentRepository {
    List<UserRole> findRoles(Long userId);

    List<UserPermission> findPermissions(Long userId);

    UserRole insertRole(UserRole assignment);

    UserPermission insertPermission(UserPermission assignment);

    void saveRoleRevocation(UserRole assignment, String reason);

    void savePermissionRevocation(UserPermission assignment, String reason);
}
