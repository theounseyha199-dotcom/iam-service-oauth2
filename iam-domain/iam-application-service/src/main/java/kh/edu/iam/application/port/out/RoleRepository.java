package kh.edu.iam.application.port.out;

import kh.edu.iam.Role;
import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.role.query.RoleFilterQuery;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    Optional<Role> findById(Long id);

    Optional<Role> findByName(String name);

    Role save(Role role);

    Optional<Role> findByUuid(UUID uuid);

    boolean existsByName(String name);

    PageResult<Role> findAll(RoleFilterQuery query);
}
