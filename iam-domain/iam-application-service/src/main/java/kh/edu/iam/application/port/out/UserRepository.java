package kh.edu.iam.application.port.out;

import kh.edu.iam.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findByUuid(UUID uuid);
    Optional <User> findByUsernameorEmail(String usernameOrEmail);
    boolean existsByEmail(String email);
 }
