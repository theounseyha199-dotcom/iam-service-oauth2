package kh.edu.iam.web.federation;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import kh.edu.iam.IdentityProvider;
import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.application.identity.command.FederatedIdentityCommand;
import kh.edu.iam.application.identity.result.PendingFederation;
import kh.edu.iam.application.identity.usecase.FindOrProvisionFederatedUserUseCase;
import kh.edu.iam.application.identity.usecase.ListUserIdentitiesUseCase;
import kh.edu.iam.web.view.BrowserUser;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Browser-only federation endpoints; all state-changing requests retain Spring CSRF checks.
 */
@Controller
public final class FederationController {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FederationController.class);
    private final BrowserUser current;
    private final FindOrProvisionFederatedUserUseCase provision;
    private final ListUserIdentitiesUseCase identities;
    private final AuthenticationSuccessHandler handler;
    private final OAuth2AuthorizationRequestResolver resolver;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> requests;

    public FederationController(
            BrowserUser current,
            FindOrProvisionFederatedUserUseCase provision,
            ListUserIdentitiesUseCase identities,
            @Qualifier("federationCompletionHandler") AuthenticationSuccessHandler handler,
            OAuth2AuthorizationRequestResolver resolver,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> requests) {
        this.current = current;
        this.provision = provision;
        this.identities = identities;
        this.handler = handler;
        this.resolver = resolver;
        this.requests = requests;
    }

    @GetMapping("/profile/accounts")
    public String accounts(HttpServletRequest request, Model model) {
        var owner = current.current();
        model.addAttribute("identities", identities.execute(owner.uuid()));
        model.addAttribute("linked", request.getParameterMap().containsKey("linked"));
        model.addAttribute("failed", request.getParameterMap().containsKey("error"));
        var session = request.getSession(false);
        if (session != null) {
            var failure = session.getAttribute("iam_federation_link_error");
            session.removeAttribute("iam_federation_link_error");
            if (request.getParameterMap().containsKey("error") && failure instanceof String code
                    && java.util.Set.of("provider_link_cancelled", "provider_link_conflict",
                            "provider_link_session", "provider_link_failed").contains(code)) {
                model.addAttribute("errorCode", code);
            }
        }
        model.addAttribute("unlinked", request.getParameterMap().containsKey("unlinked"));
        return "linked-accounts";
    }

    @PostMapping("/profile/accounts/link/{provider}")
    public void link(
            @PathVariable("provider") String provider, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        var owner = current.current();
        try {
            IdentityProvider.valueOf(provider.toUpperCase(Locale.ROOT));
            if (!provider.equals(provider.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        request.getSession().removeAttribute(PendingFederation.SESSION_ATTRIBUTE);
        request.getSession().removeAttribute("iam_federation_link_error");
        var authorization =
                OAuth2AuthorizationRequest.from(resolver.resolve(request, provider))
                        .attributes(attributes -> attributes.put("iam_link_owner", owner.uuid().toString()))
                        .build();
        requests.saveAuthorizationRequest(authorization, request, response);
        response.sendRedirect(authorization.getAuthorizationRequestUri());
    }

    @GetMapping("/federation/complete")
    public String complete(HttpServletRequest request, Model model) {
        var pending = pending(request);
        if (pending == null) {
            return "redirect:/login?oauth2error";
        }
        return form(request, model, pending, "", Map.of());
    }

    @PostMapping("/federation/complete")
    public String complete(
            @RequestParam("flow") String flow,
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "givenName", defaultValue = "") String givenName,
            @RequestParam(name = "familyName", defaultValue = "") String familyName,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model)
            throws IOException, ServletException {
        var session = request.getSession(false);
        if (session == null) {
            return "redirect:/login?oauth2error";
        }
        synchronized (session) {
            var pending = pending(request);
            if (pending == null || !pending.flow().equals(flow)) {
                return "redirect:/login?oauth2error";
            }
            var original = pending.identity();
            var values = Map.of("email", email, "givenName", givenName, "familyName", familyName);
            var errors = new LinkedHashMap<String, String>();
            if (original.email() == null) {
                try {
                    kh.edu.iam.domain.rule.Checks.email(email);
                } catch (kh.edu.iam.domain.exception.DomainException ex) {
                    errors.put("email", "validation.email");
                }
            }
            if (original.givenName() == null && (givenName.isBlank() || givenName.strip().length() > 100)) {
                errors.put("givenName", "validation.name");
            }
            if (original.familyName() == null && (familyName.isBlank() || familyName.strip().length() > 100)) {
                errors.put("familyName", "validation.name");
            }
            if (!errors.isEmpty()) {
                response.setStatus(400);
                model.addAttribute("fieldErrors", errors);
                return form(request, model, pending, "validation", values);
            }
            try {
                var command =
                        new FederatedIdentityCommand(
                                original.provider(),
                                original.providerSubject(),
                                original.email() == null ? blank(email) : original.email(),
                                original.emailVerified(),
                                original.displayName(),
                                original.username(),
                                original.pictureUrl(),
                                original.attributes(),
                                original.givenName() == null ? blank(givenName) : original.givenName(),
                                original.familyName() == null ? blank(familyName) : original.familyName(),
                                original.locale());
                var user = provision.execute(command);
                session.removeAttribute(PendingFederation.SESSION_ATTRIBUTE);
                request.changeSessionId();
                handler.onAuthenticationSuccess(
                        request, response, UsernamePasswordAuthenticationToken.unauthenticated(user, null));
                return null;
            } catch (RuntimeException ex) {
                LOG.warn("Federated profile completion failed ({})", ex.getClass().getSimpleName());
                // Do not expose provider data, uniqueness details or exception text to the browser.
                String error =
                        ex instanceof ApplicationException app && "identity_link_required".equals(app.code())
                                ? "identity_link_required"
                                : "validation";
                response.setStatus(400);
                return form(
                        request,
                        model,
                        pending,
                        error,
                        Map.of("email", email, "givenName", givenName, "familyName", familyName));
            }
        }
    }

    @PostMapping("/federation/cancel")
    public String cancel(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(PendingFederation.SESSION_ATTRIBUTE);
        }
        return "redirect:/login";
    }

    private PendingFederation pending(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) {
            return null;
        }
        if (current.isSignedIn()) {
            session.removeAttribute(PendingFederation.SESSION_ATTRIBUTE);
            return null;
        }
        var value = (PendingFederation) session.getAttribute(PendingFederation.SESSION_ATTRIBUTE);
        if (value == null || !value.expiresAt().isAfter(Instant.now())) {
            session.removeAttribute(PendingFederation.SESSION_ATTRIBUTE);
            return null;
        }
        return value;
    }

    private String form(
            HttpServletRequest request,
            Model model,
            PendingFederation pending,
            String error,
            Map<String, String> values) {
        if (!model.containsAttribute("fieldErrors")) {
            model.addAttribute("fieldErrors", Map.of());
        }
        model.addAttribute("flow", pending.flow());
        model.addAttribute("error", error);
        var identity = pending.identity();
        model.addAttribute("email", identity.email() == null ? values.getOrDefault("email", "") : identity.email());
        model.addAttribute(
                "givenName",
                identity.givenName() == null ? values.getOrDefault("givenName", "") : identity.givenName());
        model.addAttribute(
                "familyName",
                identity.familyName() == null ? values.getOrDefault("familyName", "") : identity.familyName());
        model.addAttribute("needEmail", identity.email() == null);
        model.addAttribute("needGivenName", identity.givenName() == null);
        model.addAttribute("needFamilyName", identity.familyName() == null);
        return "federation-complete";
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
