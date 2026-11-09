package kh.edu.iam.infrastructure.config;

import kh.edu.iam.IdentityProvider;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.user.result.AuthUserResult;
import kh.edu.iam.infrastructure.federation.FederationAuthorizationRequests;
import kh.edu.iam.infrastructure.federation.FederationLoginHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.EnumSet;

@Configuration(proxyBeanMethods = false)
public class FederationConfig {
    @Bean
    public AuthenticationSuccessHandler federationCompletionHandler(FederationLoginHandler handler) {
        return (request, response, authentication) ->
                handler.authenticate(request, response, (AuthUserResult) authentication.getPrincipal());
    }

    @Bean
    public FederationAuthorizationRequests federationAuthorizationRequests(CurrentActorProvider actors) {
        return new FederationAuthorizationRequests(actors);
    }

    @Bean
    public OAuth2AuthorizationRequestResolver federationAuthorizationRequestResolver(
            ClientRegistrationRepository registrations) {
        var resolver = new DefaultOAuth2AuthorizationRequestResolver(registrations, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(
                builder -> {
                    String registration = builder.build().getAttribute("registration_id");
                    if (EnumSet.of(IdentityProvider.GOOGLE, IdentityProvider.GITHUB, IdentityProvider.TELEGRAM).stream()
                            .anyMatch(provider -> provider.name().equalsIgnoreCase(registration))) {
                        OAuth2AuthorizationRequestCustomizers.withPkce().accept(builder);
                    }
                });
        return resolver;
    }
}
