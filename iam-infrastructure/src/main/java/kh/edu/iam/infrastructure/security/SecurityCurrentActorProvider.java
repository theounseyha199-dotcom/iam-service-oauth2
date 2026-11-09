package kh.edu.iam.infrastructure.security;

import kh.edu.iam.application.port.out.CurrentActorProvider;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentActorProvider implements CurrentActorProvider {

    private final AuthenticationTrustResolver trustResolver =
            new AuthenticationTrustResolverImpl();

    @Override
    public String currentActor() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || trustResolver.isAnonymous(authentication)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "An authenticated actor is required"
            );
        }

        String actor = authentication.getName();

        if (actor == null || actor.isBlank() || actor.length() > 100) {
            throw new IllegalStateException(
                    "Actor must contain 1 to 100 characters"
            );
        }

        return actor;
    }
}
