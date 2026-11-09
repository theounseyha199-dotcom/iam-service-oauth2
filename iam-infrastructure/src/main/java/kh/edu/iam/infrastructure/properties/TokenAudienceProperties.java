package kh.edu.iam.infrastructure.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;

@ConfigurationProperties("app.iam.token")
@Validated
public record TokenAudienceProperties(List<@Valid AudienceRule> audienceRules) {
    public TokenAudienceProperties {
        audienceRules = audienceRules == null ? List.of() : List.copyOf(audienceRules);
    }

    public record AudienceRule(
            String clientId, Set<@NotBlank String> anyScopes, @NotEmpty List<@NotBlank String> audiences) {
        public AudienceRule {
            clientId = clientId == null || clientId.isBlank() ? null : clientId.strip();
            anyScopes = anyScopes == null ? Set.of() : Set.copyOf(anyScopes);
            audiences = audiences == null ? List.of() : List.copyOf(audiences);
        }

        @AssertTrue(message = "Specify exactly one client-id or any-scopes selector for an audience rule")
        public boolean isSelectorValid() {
            return (clientId != null && !clientId.isBlank()) != !anyScopes.isEmpty();
        }

        public boolean matches(String requestedClient, Set<String> scopes) {
            return clientId != null ? clientId.equals(requestedClient) : anyScopes.stream().anyMatch(scopes::contains);
        }
    }
}
