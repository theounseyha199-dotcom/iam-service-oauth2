package kh.edu.iam.infrastructure.adapter;

import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.application.port.out.UserAuthenticationPort;
import kh.edu.iam.application.user.result.AuthUserResult;
import kh.edu.iam.application.user.usecase.LoadUserForAuthenticationUseCase;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public final class UserAuthenticationAdapter implements UserAuthenticationPort {
    private final LoadUserForAuthenticationUseCase load;

    @Override
    public Optional<AuthUserResult> findByUsername(String value) {
        try {
            return Optional.of(load.execute(value));
        } catch (ApplicationException ex) {
            if ("not_found".equals(ex.code())) {
                return Optional.empty();
            }
            throw ex;
        }
    }
}
