package kh.edu.iam.infrastructure.federation;

import kh.edu.iam.IdentityProvider;
import kh.edu.iam.application.identity.command.FederatedIdentityCommand;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Invoked only after Spring validates the authorization response and loads the provider user.
 */
@Component
public final class ProviderIdentityMapper {
    public FederatedIdentityCommand map(String registration, OAuth2User user) {
        IdentityProvider provider;
        try {
            provider = IdentityProvider.valueOf(registration.toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw invalid();
        }
        boolean oidc = provider == IdentityProvider.GOOGLE || provider == IdentityProvider.TELEGRAM;
        if (oidc && !(user instanceof OidcUser)) {
            throw invalid();
        }
        var attributes = user.getAttributes();
        if (oidc) {
            var token = ((OidcUser) user).getIdToken();
            String issuer = token.getIssuer() == null ? "" : token.getIssuer().toString();
            if (provider == IdentityProvider.GOOGLE
                    && !Set.of("https://accounts.google.com", "accounts.google.com").contains(issuer)) {
                throw invalid();
            }
            if (provider == IdentityProvider.TELEGRAM && !"https://oauth.telegram.org".equals(issuer)) {
                throw invalid();
            }
            // Use the signed subject, never a user-supplied handle or email.
            if (!Objects.equals(token.getSubject(), text(attributes, "sub"))) {
                throw invalid();
            }
        }
        String subject = text(attributes, oidc ? "sub" : "id");
        if (subject == null) {
            throw invalid();
        }
        String email = text(attributes, "email");
        String given = text(attributes, provider == IdentityProvider.FACEBOOK ? "first_name" : "given_name");
        String family = text(attributes, provider == IdentityProvider.FACEBOOK ? "last_name" : "family_name");
        // GitHub and Telegram often supply only a display name; do not split/invent legal names.
        String username = text(attributes, provider == IdentityProvider.GITHUB ? "login" : "preferred_username");
        String picture = text(attributes, provider == IdentityProvider.GITHUB ? "avatar_url" : "picture");
        String locale = text(attributes, "locale");
        locale = locale != null && locale.toLowerCase(Locale.ROOT).startsWith("km") ? "km" : "en";
        return new FederatedIdentityCommand(
                provider,
                subject,
                email,
                email != null && oidc && Boolean.TRUE.equals(attributes.get("email_verified")),
                text(attributes, "name"),
                username,
                picture,
                tools.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(snapshot(attributes)),
                given,
                family,
                locale);
    }

    private static Map<String, Object> snapshot(Map<String, Object> attributes) {
        // Persist profile metadata only; exclude protocol tokens, nonces, and arbitrary provider privileges.
        var result = new LinkedHashMap<String, Object>();
        for (String key :
                List.of(
                        "sub",
                        "id",
                        "name",
                        "email",
                        "email_verified",
                        "given_name",
                        "family_name",
                        "first_name",
                        "last_name",
                        "login",
                        "preferred_username",
                        "avatar_url",
                        "locale")) {
            Object value = attributes.get(key);
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                result.put(key, value);
            }
        }
        return result;
    }

    private static String text(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof Number && key.equals("id")) {
            return value.toString();
        }
        return value instanceof String s && !s.isBlank() ? s : null;
    }

    private static OAuth2AuthenticationException invalid() {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_provider_identity"));
    }
}
