package kh.edu.iam.infrastructure.federation;

import kh.edu.iam.application.port.out.EmailVerification;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.application.identity.result.PendingFederation;
import kh.edu.iam.application.identity.usecase.FindOrProvisionFederatedUserUseCase;
import kh.edu.iam.application.identity.usecase.LinkIdentityUseCase;
import kh.edu.iam.application.user.result.AuthUserResult;
import kh.edu.iam.application.user.usecase.GetUserUseCase;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

@Component
public final class FederationLoginHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FederationLoginHandler.class);
    public static final String PENDING = PendingFederation.SESSION_ATTRIBUTE;

    private final Function<AuthUserResult, UserDetails> principals;
    private final EmailVerification verification;
    private final ProviderIdentityMapper mapper;
    private final FindOrProvisionFederatedUserUseCase provision;
    private final LinkIdentityUseCase link;
    private final GetUserUseCase users;
    private final org.springframework.context.ApplicationEventPublisher events;
    private final org.springframework.security.core.session.SessionRegistry sessions;
    private final SavedRequestAwareAuthenticationSuccessHandler success =
            new SavedRequestAwareAuthenticationSuccessHandler();
    private final HttpSessionSecurityContextRepository contexts = new HttpSessionSecurityContextRepository();

    public FederationLoginHandler(
            ProviderIdentityMapper mapper,
            FindOrProvisionFederatedUserUseCase provision,
            LinkIdentityUseCase link,
            GetUserUseCase users,
            @Qualifier("federatedUserDetailsFactory") Function<AuthUserResult, UserDetails> principals,
            EmailVerification verification,
            org.springframework.context.ApplicationEventPublisher events,
            org.springframework.security.core.session.SessionRegistry sessions) {
        this.mapper = mapper;
        this.provision = provision;
        this.link = link;
        this.users = users;
        this.principals = principals;
        this.verification = verification;
        this.events = events;
        this.sessions = sessions;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        request.getSession().removeAttribute(PENDING);
        var authorization = (OAuth2AuthorizationRequest) request.getAttribute(FederationAuthorizationRequests.REQUEST);
        boolean linking =
                authorization != null && authorization.getAttribute(FederationAuthorizationRequests.OWNER) != null;
        try {
            var oauth = (OAuth2AuthenticationToken) authentication;
            var command = mapper.map(oauth.getAuthorizedClientRegistrationId(), oauth.getPrincipal());
            if (linking) {
                Authentication original = original(request);
                if (original == null) {
                    throw new ApplicationException("link_session_changed", "Sign in again before linking");
                }
                UUID owner = UUID.fromString(authorization.getAttribute(FederationAuthorizationRequests.OWNER));
                var user = users.execute(owner);
                if (!user.enabled()
                        || !user.accountNonLocked()
                        || !user.accountNonExpired()
                        || !user.credentialsNonExpired()) {
                    throw new ApplicationException("account_unavailable", "Account cannot sign in");
                }
                // Restore internal owner before auditing the use case; never audit as the external subject.
                save(request, response, original);
                link.execute(owner, command);
                events.publishEvent(new kh.edu.iam.infrastructure.workspace.AccountSecurityEvent(owner, "social_linked"));
                response.sendRedirect(request.getContextPath() + "/profile/accounts?linked");
            } else {
                // The temporary OAuth principal must never become an IAM browser session.
                save(request, response, null);
                try {
                    authenticate(request, response, provision.execute(command));
                } catch (ApplicationException ex) {
                    if (!"profile_completion_required".equals(ex.code())) {
                        throw ex;
                    }
                    request.getSession()
                            .setAttribute(
                                    PENDING,
                                    new PendingFederation(
                                            command,
                                            Instant.now().plusSeconds(300),
                                            java.util.UUID.randomUUID().toString()));
                    response.sendRedirect(request.getContextPath() + "/federation/complete");
                }
            }
        } catch (RuntimeException ex) {
            logFailure(request, ex);
            if (linking && original(request) != null) rememberLinkFailure(request, ex);
            save(request, response, linking ? original(request) : null);
            response.sendRedirect(
                    request.getContextPath()
                            + (linking && original(request) != null
                                    ? "/profile/accounts?error"
                                    : "/login?oauth2error"));
        }
    }

    public void authenticate(HttpServletRequest request, HttpServletResponse response, AuthUserResult result)
            throws IOException, ServletException {
        if (verification.isPending(result.uuid()) || !result.enabled()
                || !result.accountNonLocked()
                || !result.accountNonExpired()
                || !result.credentialsNonExpired()) {
            throw new ApplicationException("account_unavailable", "Account cannot sign in");
        }
        var principal = principals.apply(result);
        var internal = federatedAuthentication(principal);
        // Associate the final IAM principal, rather than the temporary provider principal, with this session.
        var session = request.getSession();
        request.changeSessionId();
        save(request, response, internal);
        sessions.registerNewSession(session.getId(), principal);
        events.publishEvent(new kh.edu.iam.infrastructure.workspace.AccountSecurityEvent(result.uuid(), "login"));
        success.onAuthenticationSuccess(request, response, internal);
    }

    static UsernamePasswordAuthenticationToken federatedAuthentication(UserDetails principal) {
        var authorities = new java.util.ArrayList<org.springframework.security.core.GrantedAuthority>(principal.getAuthorities());
        // Replacing the validated provider principal must retain a typed authentication factor.
        // Spring Security uses its issuedAt for OIDC auth_time whenever a session/sid is present.
        authorities.add(org.springframework.security.core.authority.FactorGrantedAuthority.fromAuthority(
                org.springframework.security.core.authority.FactorGrantedAuthority.AUTHORIZATION_CODE_AUTHORITY));
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        logFailure(request, exception);
        var session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(PENDING);
        }
        Authentication original = original(request);
        if (original != null) rememberLinkFailure(request, exception);
        save(request, response, original);
        response.sendRedirect(
                request.getContextPath() + (original == null ? "/login?oauth2error" : "/profile/accounts?error"));
    }

    private void rememberLinkFailure(HttpServletRequest request, RuntimeException exception) {
        String code = exception instanceof ApplicationException application ? application.code()
                : exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauth
                        ? oauth.getError().getErrorCode() : "";
        // Persist application-owned keys only, never provider messages or callback parameters.
        String display = switch (code == null ? "" : code) {
            case "access_denied" -> "provider_link_cancelled";
            case "conflict" -> "provider_link_conflict";
            case "link_session_changed", "authorization_request_not_found", "invalid_state_parameter" -> "provider_link_session";
            default -> "provider_link_failed";
        };
        request.getSession().setAttribute("iam_federation_link_error", display);
    }

    /** Log only bounded codes/types: exception messages can contain provider tokens or PII. */
    private void logFailure(HttpServletRequest request, RuntimeException exception) {
        String path = request.getRequestURI();
        String candidate = path.substring(path.lastIndexOf('/') + 1);
        String provider = java.util.Set.of("google", "github", "facebook", "telegram").contains(candidate)
                ? candidate : "unknown";
        String code = "callback_rejected";
        if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauth) {
            code = oauth.getError().getErrorCode();
        } else if (exception instanceof ApplicationException application) {
            code = application.code();
        }
        // OAuth error values may originate in a callback query; never log arbitrary input.
        if (code == null || !code.matches("[A-Za-z0-9_]{1,80}")) code = "provider_error";
        Throwable cause = exception;
        for (int depth = 0; depth < 10 && cause.getCause() != null; depth++) cause = cause.getCause();
        String location = java.util.Arrays.stream(cause.getStackTrace()).limit(8)
                .map(frame -> frame.getClassName() + "." + frame.getMethodName() + ":" + frame.getLineNumber())
                .collect(java.util.stream.Collectors.joining(" <- "));
        LOG.warn("Federation sign-in rejected: provider={}, code={}, exception={}, cause={}, location={}",
                provider, code, exception.getClass().getSimpleName(), cause.getClass().getSimpleName(), location);
    }

    private Authentication original(HttpServletRequest request) {
        return (Authentication) request.getAttribute(FederationAuthorizationRequests.ORIGINAL);
    }

    private void save(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
    }
}
