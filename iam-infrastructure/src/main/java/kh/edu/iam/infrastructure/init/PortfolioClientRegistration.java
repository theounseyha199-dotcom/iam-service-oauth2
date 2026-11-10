package kh.edu.iam.infrastructure.init;

import java.net.URI;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

/** Registers the portfolio only when its server-side secret is explicitly configured. */
@Component
@RequiredArgsConstructor
public class PortfolioClientRegistration implements ApplicationRunner {
    private final RegisteredClientRepository clients;
    private final PasswordEncoder encoder;

    @Value("${IAM_PORTFOLIO_CLIENT_SECRET:}")
    private String secret;

    @Value("${IAM_PORTFOLIO_ORIGIN:http://localhost:3000}")
    private String origin;

    @Override
    public void run(ApplicationArguments args) {
        if (secret.isBlank()) return;
        URI uri = URI.create(origin);
        if (uri.getHost() == null || !java.util.Set.of("http", "https").contains(uri.getScheme())
                || uri.getQuery() != null || uri.getFragment() != null || uri.getUserInfo() != null
                || !(uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
            throw new IllegalArgumentException("IAM_PORTFOLIO_ORIGIN must be an HTTP(S) origin");
        }
        String base = origin.replaceAll("/+$", "");
        String callback = base + "/api/auth/callback/iam";
        String logoutRedirect = base + "/account?signedOut=1";
        var existing = clients.findByClientId("theoun-seyha");
        if (existing != null) {
            if (!existing.getRedirectUris().contains(callback)
                    || !existing.getPostLogoutRedirectUris().contains(logoutRedirect)) {
                clients.save(RegisteredClient.from(existing).redirectUri(callback)
                        .postLogoutRedirectUri(logoutRedirect).build());
            }
            return;
        }
        clients.save(RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("theoun-seyha")
                .clientName("Seyha Portfolio")
                .clientSecret(encoder.encode(secret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(callback)
                .postLogoutRedirectUri(logoutRedirect)
                .scope("openid").scope("profile").scope("email")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true).requireAuthorizationConsent(true).build())
                .build());
    }
}
