package kh.edu.iam.restapi.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Documents protocol endpoints implemented by Spring Security's filters, not MVC controllers. */
@Configuration(proxyBeanMethods = false)
public class AuthorizationApiDocumentation {
    @Bean
    OpenApiCustomizer authorizationReference() {
        return api -> {
            api.setTags(List.of(
                    tag("Authorization", "Start here: obtain a JWT with authorization code + PKCE, then use bearerToken for API requests. Token operations authenticate the registered client, not a user password."),
                    tag("Registration", "Create a new account."),
                    tag("My Account", "Manage your own profile, password and sign-in methods."),
                    tag("Users", "Administrative user management. Requires the relevant resource:action permissions."),
                    tag("Roles", "Manage roles and their assigned permissions."),
                    tag("Permissions", "Manage individual access permissions.")));
            api.getComponents().addSecuritySchemes("clientBasic",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
                            .description("Registered confidential client ID and secret. Public PKCE clients use client_id in the form instead."));
            var authorize = operation("authorizeAccount", "Sign in and authorize access",
                    "Open this endpoint in a browser with your registered client and redirect URI. Sign in, approve consent, then exchange the returned code at /oauth2/token. This is a browser redirect flow.");
            authorize.setSecurity(List.of());
            authorize.setParameters(List.of(
                    query("response_type", true, "Use code."),
                    query("client_id", true, "Registered OAuth client ID."),
                    query("redirect_uri", true, "Must exactly match a registered redirect URI."),
                    query("scope", false, "Space-separated scopes, such as openid profile email."),
                    query("state", true, "Generate a random value and verify it on callback."),
                    query("code_challenge", true, "Base64url-encoded SHA-256 hash of the PKCE verifier."),
                    query("code_challenge_method", true, "Use S256.")));
            authorize.setResponses(new ApiResponses()
                    .addApiResponse("302", new ApiResponse().description("Redirect to sign-in, consent, or the registered callback."))
                    .addApiResponse("400", new ApiResponse().description("Invalid authorization request.")));
            api.path("/oauth2/authorize", new PathItem().get(authorize));

            var token = clientOperation("exchangeToken", "Get or refresh an access token",
                    "Send form data. For authorization_code, include code, redirect_uri and code_verifier. For refresh_token, include refresh_token. For client_credentials, authenticate a service client. Only grants enabled for the registered client are accepted; password grant is not supported.");
            token.setRequestBody(form(List.of("grant_type"),
                    "grant_type", "client_id", "code", "redirect_uri", "code_verifier", "refresh_token", "scope"));
            api.path("/oauth2/token", new PathItem().post(token));
            var revoke = clientOperation("revokeToken", "Revoke a token",
                    "Authenticate the registered client and submit a token. A successful response does not mean an already issued JWT stops validating immediately; access tokens remain subject to their expiry.");
            revoke.setRequestBody(form(List.of("token"), "token", "token_type_hint", "client_id"));
            api.path("/oauth2/revoke", new PathItem().post(revoke));
            var introspect = clientOperation("inspectToken", "Check token status",
                    "Authenticate an authorized confidential client and submit the token to inspect. The response includes active.");
            introspect.setSecurity(List.of(new SecurityRequirement().addList("clientBasic")));
            introspect.setRequestBody(form(List.of("token"), "token", "token_type_hint"));
            api.path("/oauth2/introspect", new PathItem().post(introspect));
            var keys = operation("getSigningKeys", "Get public signing keys",
                    "Public keys used to verify tokens. Private signing keys are never returned.");
            keys.setSecurity(List.of());
            api.path("/oauth2/jwks", new PathItem().get(keys));
        };
    }

    private static Tag tag(String name, String description) {
        return new Tag().name(name).description(description);
    }

    private static Operation operation(String id, String summary, String description) {
        return new Operation().operationId(id).summary(summary).description(description)
                .tags(List.of("Authorization"))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("Request completed."))
                        .addApiResponse("400", new ApiResponse().description("Invalid request."))
                        .addApiResponse("401", new ApiResponse().description("Client authentication failed.")));
    }

    private static Operation clientOperation(String id, String summary, String description) {
        return operation(id, summary, description).security(List.of(
                new SecurityRequirement().addList("clientBasic"), new SecurityRequirement()));
    }

    private static Parameter query(String name, boolean required, String description) {
        return new Parameter().name(name).in("query").required(required)
                .description(description).schema(new StringSchema());
    }

    private static RequestBody form(List<String> required, String... fields) {
        var schema = new ObjectSchema();
        for (String field : fields) {
            schema.addProperty(field, new StringSchema());
        }
        schema.setRequired(required);
        return new RequestBody().required(true).content(new Content().addMediaType(
                "application/x-www-form-urlencoded", new MediaType().schema(schema)));
    }
}
