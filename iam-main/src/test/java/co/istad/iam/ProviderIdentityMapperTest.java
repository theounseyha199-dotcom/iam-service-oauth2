package co.istad.iam;

import static org.junit.jupiter.api.Assertions.*;

import kh.edu.iam.IdentityProvider;
import kh.edu.iam.infrastructure.federation.ProviderIdentityMapper;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;

class ProviderIdentityMapperTest {
    private final ProviderIdentityMapper mapper = new ProviderIdentityMapper();
    private final List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_EXTERNAL"));

    private DefaultOidcUser oidc(String issuer) {
        var token =
                OidcIdToken.withTokenValue("unit-test-provider-token")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(60))
                        .subject("provider-subject")
                        .issuer(issuer)
                        .claim("email", "dara@example.com")
                        .claim("email_verified", true)
                        .claim("given_name", "Dara")
                        .claim("family_name", "Sok")
                        .claim("access_token", "must-not-persist")
                        .claim("roles", List.of("ADMIN"))
                        .build();
        return new DefaultOidcUser(authorities, token);
    }

    @Test
    void googleUsesTheOidcSubjectAndPersistsOnlyProfileMetadata() {
        var result = mapper.map("google", oidc("https://accounts.google.com"));
        assertEquals(IdentityProvider.GOOGLE, result.provider());
        assertEquals("provider-subject", result.providerSubject());
        assertTrue(result.emailVerified());
        assertFalse(result.attributes().contains("must-not-persist"));
        assertFalse(result.attributes().contains("ADMIN"));
    }

    @Test
    void googleRequiresAnOidcPrincipal() {
        var principal = new DefaultOAuth2User(authorities, Map.of("sub", "unverified"), "sub");
        assertThrows(OAuth2AuthenticationException.class, () -> mapper.map("google", principal));
    }

    @Test
    void googleRejectsAnUnexpectedIssuer() {
        assertThrows(OAuth2AuthenticationException.class, () -> mapper.map("google", oidc("https://attacker.example")));
    }

    @Test
    void telegramUsesItsOwnIssuerAndSubject() {
        assertEquals(IdentityProvider.TELEGRAM, mapper.map("telegram", oidc("https://oauth.telegram.org")).provider());
        assertThrows(
                OAuth2AuthenticationException.class, () -> mapper.map("telegram", oidc("https://accounts.google.com")));
    }

    @Test
    void githubUsesNumericProviderIdAndDoesNotInventVerifiedEmailOrNames() {
        var result =
                mapper.map("github", new DefaultOAuth2User(authorities, Map.of("id", 12345L, "login", "dara"), "id"));
        assertEquals("12345", result.providerSubject());
        assertNull(result.email());
        assertNull(result.givenName());
        assertNull(result.familyName());
        assertFalse(result.emailVerified());
    }

    @Test
    void facebookUsesItsAuthenticatedIdAndExplicitNameFields() {
        var result =
                mapper.map(
                        "facebook",
                        new DefaultOAuth2User(
                                authorities,
                                Map.of(
                                        "id",
                                        "fb-123",
                                        "first_name",
                                        "Dara",
                                        "last_name",
                                        "Sok",
                                        "email",
                                        "dara@example.com"),
                                "id"));
        assertEquals("fb-123", result.providerSubject());
        assertEquals("Dara", result.givenName());
        assertFalse(result.emailVerified());
    }

    @Test
    void unknownProvidersAreRejected() {
        assertThrows(
                OAuth2AuthenticationException.class, () -> mapper.map("unknown", oidc("https://accounts.google.com")));
    }
}
