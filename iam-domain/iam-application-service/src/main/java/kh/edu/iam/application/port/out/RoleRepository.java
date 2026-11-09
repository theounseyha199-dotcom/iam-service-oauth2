package kh.edu.iam.application.port.out;

import kh.edu.iam.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findByUuid(UUID uuid);

    boolean existsByName(String name);
}