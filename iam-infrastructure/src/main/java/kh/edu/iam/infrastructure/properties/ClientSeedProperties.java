package kh.edu.iam.infrastructure.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;
import java.util.Set;

@ConfigurationProperties("app.iam.clients")
@Validated
public record ClientSeedProperties(@NotNull @Valid Oidc oidc, @NotEmpty List<@Valid ServiceClient> services) {
    public ClientSeedProperties {
        services = services == null ? List.of() : List.copyOf(services);
    }

    @Override
    public String toString() {
        return "ClientSeedProperties[redacted]";
    }

    public record Oidc(
            @NotBlank String id,
            @NotBlank String secret,
            @NotEmpty Set<@NotBlank String> scopes,
            @NotEmpty List<@NotNull URI> redirectUris,
            @NotEmpty List<@NotNull URI> logoutRedirectUris) {
        public Oidc {
            scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
            redirectUris = redirectUris == null ? List.of() : List.copyOf(redirectUris);
            logoutRedirectUris = logoutRedirectUris == null ? List.of() : List.copyOf(logoutRedirectUris);
        }

        @AssertTrue(message = "Client redirect URIs must be absolute HTTP(S) URLs")
        public boolean isRedirectConfigurationValid() {
            return java.util.stream.Stream.concat(redirectUris.stream(), logoutRedirectUris.stream())
                    .allMatch(uri -> uri.getHost() != null && Set.of("http", "https").contains(uri.getScheme()));
        }

        @Override
        public String toString() {
            return "Oidc[redacted]";
        }
    }

    public record ServiceClient(@NotBlank String id, @NotBlank String secret, @NotEmpty Set<@NotBlank String> scopes) {
        public ServiceClient {
            scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
        }

        @Override
        public String toString() {
            return "ServiceClient[redacted]";
        }
    }
}
