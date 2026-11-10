package kh.edu.iam.infrastructure.federation;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.JWKSet;
import kh.edu.iam.infrastructure.security.IamUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;

class FederationTokenTest {
    @Test
    void federatedSessionIssuesIdTokenWithAuthTimeAndSid() throws Exception {
        var principal = new IamUserPrincipal(UUID.randomUUID(), "google-user", Set.of("ROLE_USER"), true, true, true, true);
        var authentication = FederationLoginHandler.federatedAuthentication(principal);
        var key = new RSAKeyGenerator(2048).keyID("test-key").generate();
        var generator = new JwtGenerator(new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key))));
        var client = RegisteredClient.withId("test-client").clientId("portfolio")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:3000/callback").scope("openid").build();
        var request = org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("http://localhost:9000/oauth2/authorize").clientId("portfolio")
                .redirectUri("http://localhost:3000/callback").scopes(Set.of("openid")).build();
        var authorization = org.springframework.security.oauth2.server.authorization.OAuth2Authorization.withRegisteredClient(client)
                .principalName(authentication.getName()).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .attribute(org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.class.getName(), request).build();
        var context = DefaultOAuth2TokenContext.builder().authorization(authorization).registeredClient(client).principal(authentication)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizedScopes(Set.of("openid"))
                .tokenType(new OAuth2TokenType("id_token"))
                .put(SessionInformation.class, new SessionInformation(principal, "hashed-session-id", new java.util.Date()))
                .authorizationServerContext(new AuthorizationServerContext() {
                    public String getIssuer() { return "http://localhost:9000"; }
                    public AuthorizationServerSettings getAuthorizationServerSettings() {
                        return AuthorizationServerSettings.builder().issuer(getIssuer()).build();
                    }
                }).build();
        // Before the fix this throws: IllegalArgumentException: authenticationTime cannot be null.
        var token = assertDoesNotThrow(() -> generator.generate(context));
        assertNotNull(token);
        assertEquals("hashed-session-id", token.getClaimAsString("sid"));
        assertNotNull(token.getClaimAsInstant("auth_time"));
        assertTrue(token.getClaimAsInstant("auth_time").isAfter(Instant.now().minusSeconds(10)));
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
