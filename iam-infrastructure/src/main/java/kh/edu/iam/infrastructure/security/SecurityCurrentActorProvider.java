package kh.edu.iam.infrastructure.security;

import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class SecurityCurrentActorProvider implements CurrentActorProvider {
    private final UserRepository users;

    @Override
    public String currentActor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        if (authentication.getPrincipal() instanceof IamUserPrincipal user) {
            return user.uuid().toString();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        if (authentication.getPrincipal() instanceof OAuth2User) {
            return "external";
        }
        return users.findByUsernameOrEmail(authentication.getName())
                .map(user -> user.getUuid().toString())
                .orElse(authentication.getName());
    }
}
