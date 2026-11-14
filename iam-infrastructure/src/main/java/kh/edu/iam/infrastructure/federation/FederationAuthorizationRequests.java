package kh.edu.iam.infrastructure.federation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import kh.edu.iam.application.port.out.CurrentActorProvider;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Link intent is bound to the server-stored OAuth request/state, not callback parameters.
 */
@RequiredArgsConstructor
public final class FederationAuthorizationRequests
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public static final String OWNER = "iam_link_owner";
    public static final String REQUEST = FederationAuthorizationRequests.class.getName() + ".request";
    public static final String ORIGINAL = FederationAuthorizationRequests.class.getName() + ".original";
    private final CurrentActorProvider actors;

    private final HttpSessionOAuth2AuthorizationRequestRepository delegate =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return delegate.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest value, HttpServletRequest request, HttpServletResponse response) {
        delegate.saveAuthorizationRequest(value, request, response);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request, HttpServletResponse response) {
        var value = delegate.removeAuthorizationRequest(request, response);
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        // An invalid callback must not log out an existing internal account. This only
        // preserves its session on failure; no authorization request means no link intent.
        if (value == null && current != null && current.isAuthenticated()
                && current.getPrincipal() instanceof kh.edu.iam.infrastructure.security.IamUserPrincipal) {
            request.setAttribute(ORIGINAL, current);
        }
        if (value != null) {
            request.setAttribute(REQUEST, value);
            String owner = value.getAttribute(OWNER);
            if (owner != null && current != null && current.isAuthenticated() && owner.equals(actors.currentActor())) {
                request.setAttribute(ORIGINAL, current);
            }
        }
        return value;
    }
}
