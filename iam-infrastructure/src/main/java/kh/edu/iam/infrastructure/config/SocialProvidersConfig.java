package kh.edu.iam.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
public class SocialProvidersConfig {
    public static final class Registrations implements ClientRegistrationRepository, Iterable<ClientRegistration> {
        private final Map<String, ClientRegistration> clients;

        Registrations(Map<String, ClientRegistration> clients) {
            this.clients = Map.copyOf(clients);
        }

        @Override
        public ClientRegistration findByRegistrationId(String id) {
            return clients.get(id);
        }

        @Override
        public Iterator<ClientRegistration> iterator() {
            return clients.values().iterator();
        }
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(Environment env) {
        var clients = new LinkedHashMap<String, ClientRegistration>();
        for (String provider : List.of("google", "github", "facebook", "telegram")) {
            String prefix = provider.toUpperCase(Locale.ROOT);
            String id = env.getProperty(prefix + "_CLIENT_ID");
            String secret = env.getProperty(prefix + "_CLIENT_SECRET");
            if (id == null || id.isBlank() || secret == null || secret.isBlank()) {
                continue;
            }
            String redirect = env.getProperty(prefix + "_REDIRECT_URI", "{baseUrl}/login/oauth2/code/{registrationId}");
            var b =
                    ClientRegistration.withRegistrationId(provider)
                            .clientId(id)
                            .clientSecret(secret)
                            .clientName(provider)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                            .redirectUri(redirect);
            switch (provider) {
                case "google" ->
                        b.scope("openid", "profile", "email")
                                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                                .tokenUri("https://oauth2.googleapis.com/token")
                                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                                .issuerUri("https://accounts.google.com")
                                .userNameAttributeName("sub");
                case "github" ->
                        b.scope("read:user", "user:email")
                                .authorizationUri("https://github.com/login/oauth/authorize")
                                .tokenUri("https://github.com/login/oauth/access_token")
                                .userInfoUri("https://api.github.com/user")
                                .userNameAttributeName("id");
                case "facebook" ->
                        // Collect email through the existing profile-completion flow.
                        // This Meta app does not have access to the email permission.
                        b.scope("public_profile")
                                .authorizationUri("https://www.facebook.com/dialog/oauth")
                                .tokenUri("https://graph.facebook.com/oauth/access_token")
                                .userInfoUri("https://graph.facebook.com/me?fields=id,name,first_name,last_name")
                                .userNameAttributeName("id")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);
                case "telegram" ->
                        b.scope("openid", "profile")
                                .authorizationUri("https://oauth.telegram.org/auth")
                                .tokenUri("https://oauth.telegram.org/token")
                                .jwkSetUri("https://oauth.telegram.org/.well-known/jwks.json")
                                .issuerUri("https://oauth.telegram.org")
                                .userNameAttributeName("sub");
                default -> throw new IllegalStateException();
            }
            clients.put(provider, b.build());
        }
        return new Registrations(clients);
    }
}
