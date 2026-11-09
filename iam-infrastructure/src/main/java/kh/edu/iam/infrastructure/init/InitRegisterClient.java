package kh.edu.iam.infrastructure.init;

import kh.edu.iam.infrastructure.properties.ClientSeedProperties;

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

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InitRegisterClient implements ApplicationRunner {
    private final RegisteredClientRepository clients;
    private final PasswordEncoder encoder;
    private final ClientSeedProperties properties;

    @Value("${app.iam.issuer}")
    private String issuer;

    @Override
    public void run(ApplicationArguments args) {
        var oidc = properties.oidc();
        var existing = clients.findByClientId(oidc.id());
        if (existing == null) {
            clients.save(
                    RegisteredClient.withId(UUID.randomUUID().toString())
                            .clientId(oidc.id())
                            .clientSecret(encoder.encode(oidc.secret()))
                            .clientAuthenticationMethod(
                                    ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                            .redirectUris(
                                    uris ->
                                            oidc.redirectUris()
                                                    .forEach(uri -> uris.add(uri.toString())))
                            .postLogoutRedirectUris(
                                    uris ->
                                            oidc.logoutRedirectUris()
                                                    .forEach(uri -> uris.add(uri.toString())))
                            .scopes(scopes -> scopes.addAll(oidc.scopes()))
                            .clientSettings(
                                    ClientSettings.builder()
                                            .requireProofKey(true)
                                            .requireAuthorizationConsent(true)
                                            .build())
                            .build());
        } else {
            var configuredRedirects = oidc.redirectUris().stream().map(Object::toString).toList();
            if (!existing.getRedirectUris().containsAll(configuredRedirects)) {
                // Apply explicitly configured callbacks to clients seeded before this startup.
                clients.save(
                        RegisteredClient.from(existing)
                                .redirectUris(uris -> uris.addAll(configuredRedirects))
                                .build());
            }
        }
        registerSwaggerClient();
        for (var service : properties.services()) {
            if (clients.findByClientId(service.id()) != null) {
                continue;
            }
            clients.save(
                    RegisteredClient.withId(UUID.randomUUID().toString())
                            .clientId(service.id())
                            .clientSecret(encoder.encode(service.secret()))
                            .clientAuthenticationMethod(
                                    ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                            .scopes(scopes -> scopes.addAll(service.scopes()))
                            .build());
        }
    }

    private void registerSwaggerClient() {
        String callback = issuer.replaceAll("/+$", "") + "/swagger-ui/oauth2-redirect.html";
        var existing = clients.findByClientId("iam-swagger");
        if (existing == null) {
            clients.save(
                    RegisteredClient.withId(UUID.randomUUID().toString())
                            .clientId("iam-swagger")
                            .clientName("IAM Swagger UI")
                            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .redirectUri(callback)
                            .scopes(scopes -> scopes.addAll(properties.oidc().scopes()))
                            .clientSettings(
                                    ClientSettings.builder()
                                            .requireProofKey(true)
                                            .requireAuthorizationConsent(true)
                                            .build())
                            .build());
        } else if (!existing.getRedirectUris().equals(java.util.Set.of(callback))) {
            clients.save(
                    RegisteredClient.from(existing)
                            .redirectUris(
                                    uris -> {
                                        uris.clear();
                                        uris.add(callback);
                                    })
                            .build());
        }
    }
}
