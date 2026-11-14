package kh.edu.iam.infrastructure.workspace;

import java.util.UUID;

import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.user.usecase.GetUserAuthoritiesUseCase;
import kh.edu.iam.application.user.usecase.GetUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Resolves access from current persisted permissions, rather than stale browser claims. */
@Component
@RequiredArgsConstructor
public final class WorkspaceAccess {
    private final CurrentActorProvider actors;
    private final GetUserUseCase users;
    private final GetUserAuthoritiesUseCase authorities;

    public UUID currentUser() {
        UUID id;
        try {
            id = UUID.fromString(actors.currentActor());
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException("Internal user required");
        }
        var user = users.execute(id);
        if (!user.enabled() || !user.accountNonLocked() || !user.accountNonExpired()
                || !user.credentialsNonExpired()) {
            throw new AccessDeniedException("Account unavailable");
        }
        return id;
    }

    public UUID require(String permission) {
        UUID id = currentUser();
        if (!authorities.execute(id).effectivePermissions().contains(permission)) {
            throw new AccessDeniedException("Forbidden");
        }
        return id;
    }
}
