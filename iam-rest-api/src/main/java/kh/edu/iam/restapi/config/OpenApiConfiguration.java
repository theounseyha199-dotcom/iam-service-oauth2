package kh.edu.iam.restapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {
    @Bean
    OpenAPI iamOpenApi() {
        var scopes =
                new Scopes()
                        .addString("openid", "Sign in")
                        .addString("profile", "Read profile claims")
                        .addString("email", "Read email claims");
        return new OpenAPI()
                .info(
                        new Info()
                                .title("ISTAD IAM API")
                                .version("1.0")
                                .description(
                                        "Click Authorize and sign in with your IAM account."
                                            + " Administrative operations require resource:action"
                                            + " permissions. 401 means invalid authentication; 403"
                                            + " means insufficient permission."))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "iamOAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.OAUTH2)
                                                .flows(
                                                        new OAuthFlows()
                                                                .authorizationCode(
                                                                        new OAuthFlow()
                                                                                .authorizationUrl(
                                                                                        "/oauth2/authorize")
                                                                                .tokenUrl(
                                                                                        "/oauth2/token")
                                                                                .scopes(scopes))))
                                .addSecuritySchemes(
                                        "bearerToken",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("iamOAuth", List.of("openid", "profile", "email")))
                .addSecurityItem(new SecurityRequirement().addList("bearerToken"));
    }

    @Bean
    OpenApiCustomizer publicRegistrationNeedsNoToken() {
        return api -> {
            var registration = api.getPaths().get("/api/auth/register");
            if (registration != null && registration.getPost() != null) {
                registration.getPost().setSecurity(List.of());
            }
        };
    }
}
