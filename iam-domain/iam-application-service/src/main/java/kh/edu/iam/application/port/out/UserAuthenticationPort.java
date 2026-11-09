package kh.edu.iam.application.port.out;

import kh.edu.iam.application.user.result.AuthUserResult;

import java.util.Optional;

public interface UserAuthenticationPort {
    Optional<AuthUserResult> findByUsername(String username);
}
