package kh.edu.iam.application.port.out;

import kh.edu.iam.User;
import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.user.query.UserFilterQuery;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);

    Optional<User> findByUuid(UUID uuid);

    Optional<User> findById(Long id);

    Optional<User> lockByUuid(UUID uuid);

    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    Optional<User> lockByUsernameOrEmail(String usernameOrEmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    PageResult<User> findAll(UserFilterQuery query);
}
