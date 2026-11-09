package kh.edu.iam.application.port.out;

import kh.edu.iam.Permission;
import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.permission.query.PermissionFilterQuery;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {

    Permission save(Permission permission);

    Optional<Permission> findByUuid(UUID uuid);

    boolean existsByName(String name);

    PageResult<Permission> findAll(PermissionFilterQuery query);
}