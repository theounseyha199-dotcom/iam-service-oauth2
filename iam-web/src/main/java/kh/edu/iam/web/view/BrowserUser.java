package kh.edu.iam.web.view;

import kh.edu.iam.application.port.out.EmailVerification;

import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.user.result.UserResult;
import kh.edu.iam.application.user.usecase.GetUserUseCase;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BrowserUser {
    private final CurrentActorProvider actors;
    private final GetUserUseCase users;
    private final EmailVerification verification;

    public boolean isSignedIn() {
        try {
            UUID.fromString(actors.currentActor());
            return true;
        } catch (IllegalArgumentException | NullPointerException ex) {
            return false;
        }
    }

    public UserResult current() {
        UUID id;
        try {
            id = UUID.fromString(actors.currentActor());
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException("Internal browser user required");
        }
        var user = users.execute(id);
        if (verification.isPending(user.uuid()) || !user.enabled() || !user.accountNonLocked() || !user.accountNonExpired() || !user.credentialsNonExpired()) {
            throw new AccessDeniedException("Account unavailable");
        }
        return user;
    }
}
