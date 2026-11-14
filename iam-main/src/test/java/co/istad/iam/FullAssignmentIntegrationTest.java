package co.istad.iam;

import static org.junit.jupiter.api.Assertions.*;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import kh.edu.iam.IdentityProvider;
import kh.edu.iam.application.identity.usecase.LinkIdentityUseCase;
import kh.edu.iam.application.user.result.UserResult;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

class FullAssignmentIntegrationTest extends IamIntegrationTestSupport {
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void scalarServesTheIamSpecificationWithoutOpeningProtectedApis() throws Exception {
        var reference = send(client(), "GET", "/scalar", null, null, null);
        assertEquals(200, reference.statusCode());
        assertTrue(reference.headers().firstValue("content-type").orElse("").contains("text/html"));
        assertTrue(reference.body().contains("/v3/api-docs"));
        assertTrue(reference.body().contains("ISTAD IAM API Reference"));
        var script = send(client(), "GET", "/scalar/scalar.js", null, null, null);
        assertEquals(200, script.statusCode());
        assertTrue(script.body().length() > 1000);
        assertEquals(401, send(client(), "GET", "/api/me", null, null, null).statusCode());
    }

    @Test
    void swaggerDocumentsApiAndUsesPublicPkceClient() throws Exception {
        var ui = send(client(), "GET", "/swagger-ui/index.html", null, null, null);
        assertEquals(200, ui.statusCode());
        var callback = send(client(), "GET", "/swagger-ui/oauth2-redirect.html", null, null, null);
        assertEquals(200, callback.statusCode());
        var spec = send(client(), "GET", "/v3/api-docs", null, null, null);
        assertEquals(200, spec.statusCode());
        var document = tree(spec);
        assertTrue(document.get("paths").has("/api/users"));
        assertTrue(document.get("paths").has("/api/me"));
        assertEquals(
                0,
                document.get("paths").get("/api/auth/register").get("post").get("security").size());
        assertTrue(document.get("components").get("securitySchemes").has("iamOAuth"));
        assertEquals("Users", document.at("/paths/~1api~1users/get/tags/0").asText());
        assertEquals("List users", document.at("/paths/~1api~1users/get/summary").asText());
        assertEquals("My Account", document.at("/paths/~1api~1me/get/tags/0").asText());
        assertEquals("Authorization", document.at("/tags/0/name").asText());
        assertEquals("Authorization", document.at("/paths/~1oauth2~1token/post/tags/0").asText());
        assertTrue(document.at("/paths/~1oauth2~1token/post/requestBody/content")
                .has("application/x-www-form-urlencoded"));
        assertEquals(0, document.at("/paths/~1oauth2~1authorize/get/security").size());
        var operationIds = new java.util.HashSet<String>();
        document.get("paths").properties().forEach(path ->
                path.getValue().properties().forEach(method -> {
                    var operation = method.getValue();
                    if (operation.has("operationId")) {
                        assertTrue(operationIds.add(operation.get("operationId").asText()));
                        assertTrue(operation.has("summary"));
                        assertFalse(operation.get("tags").get(0).asText().endsWith("-controller"));
                    }
                }));
        var registered =
                bean(org.springframework.security.oauth2.server.authorization.client
                                .RegisteredClientRepository.class)
                        .findByClientId("iam-swagger");
        assertNotNull(registered);
        assertNull(registered.getClientSecret());
        assertTrue(registered.getClientSettings().isRequireProofKey());
        assertTrue(registered.getClientSettings().isRequireAuthorizationConsent());
        assertTrue(
                registered
                        .getClientAuthenticationMethods()
                        .contains(
                                org.springframework.security.oauth2.core.ClientAuthenticationMethod
                                        .NONE));
        assertEquals(401, send(client(), "GET", "/api/users", null, null, null).statusCode());
    }

    private String origin() {
        return "http://127.0.0.1:" + context.getEnvironment().getProperty("local.server.port");
    }

    private HttpClient client() {
        return HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    private HttpResponse<String> send(
            HttpClient client,
            String method,
            String path,
            String body,
            String token,
            String contentType)
            throws Exception {
        var builder =
                HttpRequest.newBuilder(URI.create(path.startsWith("http") ? path : origin() + path))
                        .timeout(Duration.ofSeconds(20));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }
        builder.method(
                method,
                body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> api(
            String method, String path, Object body, String token, int expected) throws Exception {
        var response =
                send(
                        client(),
                        method,
                        path,
                        body == null ? null : json.writeValueAsString(body),
                        token,
                        "application/json");
        assertEquals(
                expected,
                response.statusCode(),
                () -> method + " " + path + ": " + response.body());
        return response;
    }

    private JsonNode tree(HttpResponse<String> response) {
        return json.readTree(response.body());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String form(Map<String, String> fields) {
        return fields.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(java.util.stream.Collectors.joining("&"));
    }

    @SuppressWarnings("unchecked")
    private String token(UUID subject, Collection<String> permissions) {
        var encoder = new NimbusJwtEncoder((JWKSource<SecurityContext>) bean(JWKSource.class));
        var claims =
                JwtClaimsSet.builder()
                        .issuer("http://localhost:9000")
                        .subject(subject.toString())
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(300))
                        .claim("scope", "openid profile email")
                        .claim("authorities", new ArrayList<>(permissions))
                        .build();
        return encoder.encode(
                        JwtEncoderParameters.from(
                                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
                .getTokenValue();
    }

    private String adminToken() {
        var names = jdbc.queryForList("select name from permissions where is_system", String.class);
        return token(register().uuid(), names);
    }

    private String reactPayload(String html) {
        var match = Pattern.compile("<script id=\"iam-page\" type=\"application/json\">(.*?)</script>", Pattern.DOTALL).matcher(html);
        assertTrue(match.find());
        return match.group(1);
    }

    private String csrf(String html) {
        var matcher = Pattern.compile("\"csrfToken\":\"([^\"]+)\"").matcher(html);
        assertTrue(matcher.find(), "A CSRF token must be rendered");
        return matcher.group(1);
    }

    @Test
    void reactBootstrapEscapesUserInputAndDoesNotExposePasswordOrMvcInternals() throws Exception {
        var browser = client();
        var login = send(browser, "GET", "/login", null, null, null);
        String injected = "</script><script>window.injected=true</script>";
        var response = send(browser, "POST", "/register", form(Map.of(
                "_csrf", csrf(login.body()), "username", injected, "email", "invalid",
                "givenName", "Test", "familyName", "User", "password", "never-echo-this-secret",
                "confirmPassword", "mismatch")), null, "application/x-www-form-urlencoded");
        assertEquals(400, response.statusCode());
        assertFalse(response.body().contains(injected));
        assertFalse(response.body().contains("never-echo-this-secret"));
        assertFalse(response.body().contains("BindingResult"));
        var payload = json.readTree(reactPayload(response.body()));
        assertEquals(injected, payload.path("data").path("values").path("username").asText());
        assertEquals("register", payload.path("page").asText());
        assertFalse(payload.path("csrfToken").asText().isBlank());
        assertEquals("no-store", response.headers().firstValue("Cache-Control").orElseThrow());
    }

    private HttpClient login(UserResult user) throws Exception {
        var browser = client();
        var page = send(browser, "GET", "/login", null, null, null);
        assertEquals(200, page.statusCode(), page.body());
        var response =
                send(
                        browser,
                        "POST",
                        "/login",
                        form(
                                Map.of(
                                        "username",
                                        user.username(),
                                        "password",
                                        PASSWORD,
                                        "_csrf",
                                        csrf(page.body()))),
                        null,
                        "application/x-www-form-urlencoded");
        assertEquals(302, response.statusCode(), response.body());
        assertFalse(response.headers().firstValue("Location").orElse("").contains("error"));
        return browser;
    }

    @Test
    void allSectionDRoutesUseTheSpecifiedPayloadsAndPermissionAuthorities() throws Exception {
        var admin = adminToken();
        var name = unique();
        var registered =
                tree(
                        api(
                                "POST",
                                "/api/auth/register",
                                Map.of(
                                        "username",
                                        name,
                                        "email",
                                        name + "@example.com",
                                        "password",
                                        PASSWORD,
                                        "confirmPassword",
                                        PASSWORD,
                                        "givenName",
                                        "Dara",
                                        "familyName",
                                        "Sok",
                                        "locale",
                                        "km"),
                                null,
                                201));
        assertFalse(registered.has("passwordHash"));
        var p =
                tree(
                        api(
                                "POST",
                                "/api/permissions",
                                Map.of(
                                        "resource",
                                        unique(),
                                        "action",
                                        "read",
                                        "description",
                                        "Read reports"),
                                admin,
                                201));
        String pid = p.get("uuid").asText();
        api("GET", "/api/permissions/" + pid, null, admin, 200);
        var page =
                tree(api("GET", "/api/permissions?page=0&size=2&sort=name,asc", null, admin, 200));
        assertTrue(page.has("content"));
        assertEquals(2, page.get("page").get("size").asInt());
        api("PATCH", "/api/permissions/" + pid, Map.of("description", "Updated"), admin, 200);
        api("POST", "/api/permissions/" + pid + "/deactivate", null, admin, 204);
        api("POST", "/api/permissions/" + pid + "/activate", null, admin, 204);
        var role =
                tree(
                        api(
                                "POST",
                                "/api/roles",
                                Map.of(
                                        "name",
                                        unique(),
                                        "description",
                                        "Content manager",
                                        "permissionUuids",
                                        List.of()),
                                admin,
                                201));
        String rid = role.get("uuid").asText();
        api("GET", "/api/roles", null, admin, 200);
        api("GET", "/api/roles/" + rid, null, admin, 200);
        api("PATCH", "/api/roles/" + rid, Map.of("description", "Updated role"), admin, 200);
        api("POST", "/api/roles/" + rid + "/deactivate", null, admin, 204);
        api("POST", "/api/roles/" + rid + "/activate", null, admin, 204);
        api(
                "POST",
                "/api/roles/" + rid + "/permissions",
                Map.of("permissionUuids", List.of(pid), "reason", "Initial access"),
                admin,
                200);
        var username = unique();
        var input = new LinkedHashMap<String, Object>();
        input.put("username", username);
        input.put("email", username + "@example.com");
        input.put("password", PASSWORD);
        input.put("givenName", "Dara");
        input.put("familyName", "Sok");
        input.put("locale", "km");
        input.put("gender", "male");
        input.put("birthdate", "2003-05-20");
        input.put("phoneNumber", "+855" + String.valueOf(System.nanoTime()).substring(4, 12));
        input.put("zoneinfo", "Asia/Phnom_Penh");
        input.put(
                "address",
                Map.of(
                        "streetAddress",
                        "No. 24",
                        "locality",
                        "Toul Kork",
                        "region",
                        "Phnom Penh",
                        "postalCode",
                        "120408",
                        "country",
                        "KH"));
        input.put("roleUuids", List.of(rid));
        var created = tree(api("POST", "/api/users", input, admin, 201));
        String uid = created.get("uuid").asText();
        assertEquals(rid, created.get("roles").get(0).get("uuid").asText());
        api("GET", "/api/users", null, admin, 200);
        api("GET", "/api/users/" + uid, null, admin, 200);
        api("PATCH", "/api/users/" + uid, Map.of("nickname", "Dara", "locale", "en"), admin, 200);
        for (String action : List.of("disable", "enable", "lock", "unlock")) {
            api("POST", "/api/users/" + uid + "/" + action, null, admin, 204);
        }
        api(
                "PUT",
                "/api/users/" + uid + "/password",
                Map.of("newPassword", "N3w!Pass2026", "confirmPassword", "N3w!Pass2026"),
                admin,
                204);
        api("GET", "/api/users/" + uid + "/authorities", null, admin, 200);
        api(
                "DELETE",
                "/api/users/" + uid + "/roles/" + rid,
                Map.of("reason", "Remove role"),
                admin,
                204);
        api(
                "POST",
                "/api/users/" + uid + "/roles",
                Map.of("roleUuids", List.of(rid), "reason", "Restore role"),
                admin,
                200);
        api(
                "POST",
                "/api/users/" + uid + "/permissions",
                Map.of("permissionUuids", List.of(pid), "effect", "DENY", "reason", "Restricted"),
                admin,
                200);
        api(
                "DELETE",
                "/api/users/" + uid + "/permissions/" + pid,
                Map.of("reason", "Remove override"),
                admin,
                204);
        api("GET", "/api/users/" + uid + "/identities", null, admin, 200);
        var linked =
                bean(LinkIdentityUseCase.class)
                        .execute(
                                UUID.fromString(uid),
                                identity(IdentityProvider.GITHUB, unique(), null));
        api("DELETE", "/api/users/" + uid + "/identities/" + linked.uuid(), null, admin, 204);
        String own = token(UUID.fromString(uid), List.of());
        api("GET", "/api/me", null, own, 200);
        api("PATCH", "/api/me", Map.of("nickname", "Self"), own, 200);
        api(
                "PUT",
                "/api/me/password",
                Map.of(
                        "currentPassword",
                        "N3w!Pass2026",
                        "newPassword",
                        PASSWORD,
                        "confirmPassword",
                        PASSWORD),
                own,
                204);
        api("GET", "/api/me/identities", null, own, 200);
        var selfLinked =
                bean(LinkIdentityUseCase.class)
                        .execute(
                                UUID.fromString(uid),
                                identity(IdentityProvider.GOOGLE, unique(), null));
        api("DELETE", "/api/me/identities/" + selfLinked.uuid(), null, own, 204);
        api(
                "DELETE",
                "/api/roles/" + rid + "/permissions/" + pid,
                Map.of("reason", "Remove permission"),
                admin,
                204);
        api("DELETE", "/api/users/" + uid, null, admin, 204);
        api("DELETE", "/api/roles/" + rid, null, admin, 204);
        api("DELETE", "/api/permissions/" + pid, null, admin, 204);
    }

    @Test
    void malformedForbiddenAndUnauthenticatedRequestsUseProblemDetails() throws Exception {
        var unauth = api("GET", "/api/users", null, null, 401);
        assertTrue(
                unauth.headers()
                        .firstValue("Content-Type")
                        .orElse("")
                        .contains("application/problem+json"));
        assertEquals(401, tree(unauth).get("status").asInt());
        String own = token(register().uuid(), List.of("ROLE_ADMIN"));
        api("GET", "/api/users", null, own, 403);
        var invalid =
                api(
                        "POST",
                        "/api/auth/register",
                        Map.of("email", "bad", "password", "hidden-secret"),
                        null,
                        400);
        assertTrue(tree(invalid).has("errors"));
        assertFalse(invalid.body().contains("hidden-secret"));
        api("GET", "/api/users/not-a-uuid", null, adminToken(), 400);
        api("GET", "/api/roles?size=10000", null, adminToken(), 400);
        api("GET", "/api/users/" + UUID.randomUUID(), null, adminToken(), 404);
        var bad =
                send(
                        client(),
                        "GET",
                        "/api/me",
                        null,
                        own.substring(0, own.length() - 10) + "badbadbad!",
                        null);
        assertEquals(401, bad.statusCode());
    }

    @Test
    void englishKhmerPagesValidationAndCsrfWorkOverHttp() throws Exception {
        var browser = client();
        var english = send(browser, "GET", "/login?lang=en", null, null, null);
        assertEquals(200, english.statusCode(), english.body());
        assertTrue(english.body().contains("lang=\"en\""));
        var khmer = send(browser, "GET", "/login?lang=km", null, null, null);
        assertEquals(200, khmer.statusCode());
        assertTrue(khmer.body().contains("lang=\"km\""));
        assertTrue(khmer.body().contains("ចូលគណនី"));
        var missingCsrf =
                send(
                        browser,
                        "POST",
                        "/register",
                        "username=no-csrf",
                        null,
                        "application/x-www-form-urlencoded");
        assertEquals(
                403,
                missingCsrf.statusCode(),
                missingCsrf.headers().map().toString() + missingCsrf.body());
        var invalid =
                send(
                        browser,
                        "POST",
                        "/register",
                        form(
                                Map.of(
                                        "username",
                                        "a",
                                        "email",
                                        "bad",
                                        "password",
                                        "short",
                                        "confirmPassword",
                                        "different",
                                        "_csrf",
                                        csrf(khmer.body()))),
                        null,
                        "application/x-www-form-urlencoded");
        assertEquals(400, invalid.statusCode(), invalid.body());
        assertFalse(json.readTree(reactPayload(invalid.body())).path("data").path("errors").isEmpty());
        assertFalse(invalid.body().contains("value=\"short\""));
        var user = register();
        browser = login(user);
        for (String route :
                List.of(
                        "/",
                        "/profile/edit",
                        "/profile/password",
                        "/profile/accounts",
                        "/profile/apps")) {
            var page = send(browser, "GET", route, null, null, null);
            assertEquals(200, page.statusCode(), () -> route + ": " + page.body());
            assertFalse(page.body().contains("passwordHash"));
            var accountData = json.readTree(reactPayload(page.body())).path("data");
            assertEquals(user.username(), accountData.path("claims").path("username").asText());
            assertTrue(accountData.path("roles").isArray());
            assertTrue(accountData.path("permissions").isArray());
        }
        var edit = send(browser, "GET", "/profile/edit", null, null, null);
        var stale =
                send(
                        browser,
                        "POST",
                        "/profile/edit",
                        form(
                                Map.of(
                                        "_csrf",
                                        csrf(edit.body()),
                                        "version",
                                        "-1",
                                        "email",
                                        user.email(),
                                        "givenName",
                                        "Dara",
                                        "familyName",
                                        "Sok",
                                        "locale",
                                        "en")),
                        null,
                        "application/x-www-form-urlencoded");
        assertEquals(409, stale.statusCode(), stale.body());
        assertTrue(stale.body().contains("concurrent_update"));
    }

    @Test
    void fourSocialProvidersStartWithStateAndSupportedProvidersUsePkce() throws Exception {
        for (String provider : List.of("google", "github", "facebook", "telegram")) {
            var response =
                    send(client(), "GET", "/oauth2/authorization/" + provider, null, null, null);
            assertEquals(302, response.statusCode(), response.body());
            String location = response.headers().firstValue("Location").orElseThrow();
            assertTrue(location.contains("state="), provider);
            assertTrue(location.contains("client_id=test-" + provider), location);
            if (provider.equals("facebook")) {
                String query = java.net.URLDecoder.decode(
                        java.net.URI.create(location).getRawQuery(), java.nio.charset.StandardCharsets.UTF_8);
                assertTrue(query.contains("scope=public_profile&"), query);
                assertFalse(query.contains("email"), query);
            }
            if (!provider.equals("facebook")) {
                assertTrue(location.contains("code_challenge="), provider);
            }
        }
    }

    @Test
    void linkedAccountAuthorizationKeepsOwnerSessionAfterProviderCancellation() throws Exception {
        var browser = login(register());
        var accounts = send(browser, "GET", "/profile/accounts", null, null, null);
        assertEquals(200, accounts.statusCode());
        var start = send(browser, "POST", "/profile/accounts/link/google",
                form(Map.of("_csrf", csrf(accounts.body()))), null, "application/x-www-form-urlencoded");
        assertEquals(302, start.statusCode(), start.body());
        var location = URI.create(start.headers().firstValue("Location").orElseThrow());
        assertEquals("accounts.google.com", location.getHost());
        String state = java.util.Arrays.stream(location.getRawQuery().split("&"))
                .filter(value -> value.startsWith("state=")).findFirst().orElseThrow().substring(6);
        var cancelled = send(browser, "GET", "/login/oauth2/code/google?error=access_denied&state=" + state,
                null, null, null);
        assertEquals(302, cancelled.statusCode());
        assertTrue(cancelled.headers().firstValue("Location").orElse("").endsWith("/profile/accounts?error"));
        var failure = send(browser, "GET", "/profile/accounts?error", null, null, null);
        assertEquals(200, failure.statusCode());
        assertEquals("provider_link_cancelled", json.readTree(reactPayload(failure.body())).path("data").path("errorCode").asText());
        var refreshed = send(browser, "GET", "/profile/accounts?error&errorCode=provider_link_conflict", null, null, null);
        assertFalse(json.readTree(reactPayload(refreshed.body())).path("data").has("errorCode"));
    }

    @Test
    void rejectedLinkCallbackDoesNotSignOutExistingOwner() throws Exception {
        var browser = login(register());
        var callback = send(browser, "GET", "/login/oauth2/code/google?code=untrusted&state=unknown",
                null, null, null);
        assertEquals(302, callback.statusCode());
        assertTrue(callback.headers().firstValue("Location").orElse("").endsWith("/profile/accounts?error"));
        var accounts = send(browser, "GET", "/profile/accounts?error", null, null, null);
        assertEquals(200, accounts.statusCode());
        assertEquals("provider_link_session", json.readTree(reactPayload(accounts.body())).path("data").path("errorCode").asText());
    }

    private record Grant(
            HttpClient browser,
            String accessToken,
            String refreshToken,
            JsonNode idClaims,
            UserResult user) {}

    private Grant authorizationCodeGrant() throws Exception {
        var user = register();
        var browser = login(user);
        String verifier =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        String challenge =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                MessageDigest.getInstance("SHA-256")
                                        .digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        String path =
                "/oauth2/authorize?response_type=code&client_id=iam-web-client&redirect_uri="
                        + encode("http://localhost:8081/login/oauth2/code/iam")
                        + "&scope="
                        + encode("openid profile email phone address")
                        + "&state=test-state&code_challenge_method=S256&code_challenge="
                        + challenge;
        var response = send(browser, "GET", path, null, null, null);
        assertEquals(302, response.statusCode(), response.body());
        String location = response.headers().firstValue("Location").orElseThrow();
        assertTrue(location.contains("/oauth2/consent"), location);
        var consent = send(browser, "GET", location, null, null, null);
        assertEquals(200, consent.statusCode(), consent.body());
        String body =
                form(Map.of("client_id", "iam-web-client", "state", query(location).get("state")))
                        + "&scope=profile&scope=email&scope=phone&scope=address";
        var allowed =
                send(
                        browser,
                        "POST",
                        "/oauth2/authorize",
                        body,
                        null,
                        "application/x-www-form-urlencoded");
        assertEquals(302, allowed.statusCode(), allowed.body());
        String callback = allowed.headers().firstValue("Location").orElseThrow();
        assertTrue(query(callback).containsKey("code"), callback);
        var exchange =
                basicPost(
                        "/oauth2/token",
                        form(
                                Map.of(
                                        "grant_type",
                                        "authorization_code",
                                        "code",
                                        query(callback).get("code"),
                                        "redirect_uri",
                                        "http://localhost:8081/login/oauth2/code/iam",
                                        "code_verifier",
                                        verifier)),
                        "iam-web-client",
                        "test-client-secret");
        assertEquals(200, exchange.statusCode(), exchange.body());
        var tokens = tree(exchange);
        var id = bean(JwtDecoder.class).decode(tokens.get("id_token").asText());
        return new Grant(
                browser,
                tokens.get("access_token").asText(),
                tokens.get("refresh_token").asText(),
                json.valueToTree(id.getClaims()),
                user);
    }

    private HttpResponse<String> basicPost(String path, String body, String id, String secret)
            throws Exception {
        return client().send(
                        HttpRequest.newBuilder(URI.create(origin() + path))
                                .header(
                                        "Authorization",
                                        "Basic "
                                                + Base64.getEncoder()
                                                        .encodeToString(
                                                                (id + ":" + secret)
                                                                        .getBytes(
                                                                                StandardCharsets
                                                                                        .UTF_8)))
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .POST(HttpRequest.BodyPublishers.ofString(body))
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
    }

    private Map<String, String> query(String uri) {
        String raw = URI.create(uri).getRawQuery();
        var result = new LinkedHashMap<String, String>();
        if (raw != null) {
            for (String part : raw.split("&")) {
                String[] pair = part.split("=", 2);
                result.put(
                        URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "");
            }
        }
        return result;
    }

    @Test
    void authorizationCodePkceScopedClaimsUserinfoAndConsentRevocation() throws Exception {
        var grant = authorizationCodeGrant();
        assertEquals(grant.user().uuid().toString(), grant.idClaims().get("sub").asText());
        assertEquals("Dara Sok", grant.idClaims().get("name").asText());
        assertEquals(grant.user().email(), grant.idClaims().get("email").asText());
        var access = bean(JwtDecoder.class).decode(grant.accessToken());
        assertTrue(access.getClaimAsStringList("authorities").contains("ROLE_USER"));
        api("GET", "/api/me", null, grant.accessToken(), 200);
        var info = send(client(), "GET", "/userinfo", null, grant.accessToken(), null);
        assertEquals(200, info.statusCode(), info.body());
        var apps = send(grant.browser(), "GET", "/profile/apps", null, null, null);
        assertEquals(200, apps.statusCode(), apps.body());
        String clientId =
                jdbc.queryForObject(
                        "select id from oauth2_registered_client where client_id='iam-web-client'",
                        String.class);
        var confirmation =
                send(
                        grant.browser(),
                        "GET",
                        "/profile/apps/" + clientId + "/revoke",
                        null,
                        null,
                        null);
        assertEquals(200, confirmation.statusCode(), confirmation.body());
        var revoked =
                send(
                        grant.browser(),
                        "POST",
                        "/profile/apps/" + clientId + "/revoke",
                        form(Map.of("_csrf", csrf(confirmation.body()))),
                        null,
                        "application/x-www-form-urlencoded");
        assertEquals(302, revoked.statusCode());
        var refresh =
                basicPost(
                        "/oauth2/token",
                        form(
                                Map.of(
                                        "grant_type",
                                        "refresh_token",
                                        "refresh_token",
                                        grant.refreshToken())),
                        "iam-web-client",
                        "test-client-secret");
        assertEquals(400, refresh.statusCode(), refresh.body());
    }

    @Test
    void dynamicClientRegistrationUsesInitialAndRegistrationAccessTokens() throws Exception {
        var issued =
                basicPost(
                        "/oauth2/token",
                        "grant_type=client_credentials&scope=client.create",
                        "iam-registration",
                        "test-dcr-secret");
        assertEquals(200, issued.statusCode(), issued.body());
        String initial = tree(issued).get("access_token").asText();
        var request =
                Map.of(
                        "client_name",
                        "ISTAD E-Commerce Web",
                        "redirect_uris",
                        List.of("http://localhost:8081/login/oauth2/code/iam"),
                        "grant_types",
                        List.of("authorization_code", "refresh_token"),
                        "response_types",
                        List.of("code"),
                        "scope",
                        "openid profile email",
                        "token_endpoint_auth_method",
                        "client_secret_basic");
        var registered =
                send(
                        client(),
                        "POST",
                        "/connect/register",
                        json.writeValueAsString(request),
                        initial,
                        "application/json");
        assertEquals(201, registered.statusCode(), registered.body());
        var result = tree(registered);
        assertTrue(result.has("client_secret"));
        assertTrue(result.has("registration_access_token"));
        String registrationPath =
                "/connect/register?client_id=" + encode(result.get("client_id").asText());
        var read =
                send(
                        client(),
                        "GET",
                        registrationPath,
                        null,
                        result.get("registration_access_token").asText(),
                        null);
        assertEquals(200, read.statusCode(), read.body());
        assertFalse(tree(read).has("client_secret"));
        assertEquals(
                401,
                send(
                                client(),
                                "POST",
                                "/connect/register",
                                json.writeValueAsString(request),
                                null,
                                "application/json")
                        .statusCode());
        var fresh =
                basicPost(
                        "/oauth2/token",
                        "grant_type=client_credentials&scope=client.create",
                        "iam-registration",
                        "test-dcr-secret");
        initial = tree(fresh).get("access_token").asText();
        var privileged = new LinkedHashMap<String, Object>(request);
        privileged.put("scope", "openid user:create");
        assertEquals(
                400,
                send(
                                client(),
                                "POST",
                                "/connect/register",
                                json.writeValueAsString(privileged),
                                initial,
                                "application/json")
                        .statusCode());
    }

    @Test
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(
            named = "iam.browser.checks",
            matches = "true")
    void realBrowserEnglishKhmerMobileAndAccountFlows() throws Exception {
        var root = java.nio.file.Path.of(System.getProperty("iam.project.root"));
        var process =
                new ProcessBuilder("node", root.resolve("scripts/browser-check.mjs").toString())
                        .directory(root.toFile())
                        .redirectErrorStream(true);
        process.environment().put("IAM_BROWSER_URL", origin());
        process.environment().put("IAM_BROWSER_USERNAME", register().username());
        process.environment().put("IAM_BROWSER_PASSWORD", PASSWORD);
        var connectedUser = register();
        for (var provider : List.of(IdentityProvider.GOOGLE, IdentityProvider.GITHUB,
                IdentityProvider.FACEBOOK, IdentityProvider.TELEGRAM)) {
            bean(LinkIdentityUseCase.class).execute(connectedUser.uuid(),
                    identity(provider, unique(), connectedUser.email()));
        }
        process.environment().put("IAM_BROWSER_LINKED_USERNAME", connectedUser.username());
        String otpName = unique();
        var otpUser = bean(kh.edu.iam.application.user.usecase.RegisterUserUseCase.class).execute(
                new kh.edu.iam.application.user.command.RegisterUserCommand(otpName, otpName + "@example.com",
                        PASSWORD, PASSWORD, "OTP", "Browser", "en"));
        var redis = bean(org.springframework.data.redis.core.StringRedisTemplate.class);
        redis.delete(List.of(kh.edu.iam.infrastructure.security.RedisVerificationCodes.key(otpUser.uuid()),
                kh.edu.iam.infrastructure.security.RedisVerificationCodes.cooldownKey(otpUser.uuid())));
        bean(kh.edu.iam.infrastructure.security.RedisVerificationCodes.class).issue(otpUser.uuid(), otpUser.email(), "123456");
        process.environment().put("IAM_BROWSER_OTP_EMAIL", otpUser.email());
        process.environment().put("IAM_BROWSER_OTP_CODE", "123456");
        var log = root.resolve("verification/browser-process.log");
        java.nio.file.Files.createDirectories(log.getParent());
        var running = process.redirectOutput(log.toFile()).start();
        if (!running.waitFor(90, java.util.concurrent.TimeUnit.SECONDS)) {
            running.destroyForcibly();
            fail("Browser verification timed out");
        }
        assertEquals(0, running.exitValue(), java.nio.file.Files.readString(log));
    }

    @Test
    void socialCallbackWithUnknownStateCannotProvisionAnAccount() throws Exception {
        Integer before = jdbc.queryForObject("select count(*) from users", Integer.class);
        var response =
                send(
                        client(),
                        "GET",
                        "/login/oauth2/code/google?code=untrusted&state=unknown",
                        null,
                        null,
                        null);
        assertEquals(302, response.statusCode());
        assertTrue(response.headers().firstValue("Location").orElse("").contains("oauth2error"));
        assertEquals(before, jdbc.queryForObject("select count(*) from users", Integer.class));
    }

    @Test
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(
            named = "iam.packaged.checks",
            matches = "true")
    void packagedJarRendersReactOutsideTheSourceTree() throws Exception {
        var directory = java.nio.file.Files.createTempDirectory("iam-packaged-check-");
        var log = directory.resolve("application.log");
        var process =
                new ProcessBuilder(
                                java.nio.file.Path.of(
                                                System.getProperty("java.home"), "bin", "java")
                                        .toString(),
                                "-jar",
                                System.getProperty("iam.packaged.jar"),
                                "--server.port=0",
                                "--spring.datasource.url="
                                        + context.getEnvironment()
                                                .getProperty("spring.datasource.url"),
                                "--spring.datasource.username="
                                        + context.getEnvironment()
                                                .getProperty("spring.datasource.username"),
                                "--spring.datasource.password=",
                                "--app.iam.clients.oidc.secret=test-client-secret",
                                "--IAM_DCR_CLIENT_SECRET=test-dcr-secret",
                                "--app.iam.signing-key-file=" + directory.resolve("key.json"))
                        .directory(directory.toFile())
                        .redirectErrorStream(true)
                        .redirectOutput(log.toFile())
                        .start();
        try {
            String port = null;
            for (int attempt = 0; attempt < 150 && process.isAlive(); attempt++) {
                String output =
                        java.nio.file.Files.exists(log) ? java.nio.file.Files.readString(log) : "";
                var matcher = Pattern.compile("Tomcat started on port ([0-9]+)").matcher(output);
                if (matcher.find()) {
                    port = matcher.group(1);
                    break;
                }
                Thread.sleep(200);
            }
            assertNotNull(port, java.nio.file.Files.readString(log));
            var response =
                    client().send(
                                    HttpRequest.newBuilder(
                                                    URI.create(
                                                            "http://127.0.0.1:"
                                                                    + port
                                                                    + "/login?lang=km"))
                                            .GET()
                                            .build(),
                                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode(), response.body());
            assertTrue(response.body().contains("ចូលគណនី"));
            assertTrue(response.body().contains("id=\"iam-page\""));
            assertTrue(response.body().contains("/react/assets/"));
        } finally {
            process.destroy();
            if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
            try (var paths = java.nio.file.Files.walk(directory)) {
                for (var path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    java.nio.file.Files.deleteIfExists(path);
                }
            }
        }
    }
    @Test
    void legacyClientLogoutRequiresCsrfAndEndsTheIamSession() throws Exception {
        var browser = login(register());
        var confirmation = send(browser, "GET", "/logout", null, null, null);
        assertEquals(200, confirmation.statusCode(), confirmation.body());
        String csrfToken = csrf(confirmation.body());
        assertEquals(403, send(browser, "POST", "/logout", "", null,
                "application/x-www-form-urlencoded").statusCode());
        var logout = send(browser, "POST", "/logout", form(Map.of("_csrf", csrfToken)), null,
                "application/x-www-form-urlencoded");
        assertEquals(302, logout.statusCode());
        var profile = send(browser, "GET", "/", null, null, null);
        assertEquals(302, profile.statusCode());
        assertTrue(profile.headers().firstValue("Location").orElse("").contains("/login"));
    }

    private UserResult workspaceAdmin() {
        var admin=register();
        bean(kh.edu.iam.application.user.usecase.AssignRolesToUserUseCase.class).execute(
                new kh.edu.iam.application.user.command.AssignRolesToUserCommand(admin.uuid(),
                        List.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),null,"Workspace fixture"));
        return admin;
    }
    private HttpResponse<String> workspace(HttpClient browser,String csrf,String method,String path,Object body) throws Exception {
        var request=HttpRequest.newBuilder(URI.create(origin()+"/workspace/api"+path))
                .header("Content-Type","application/json").header("X-CSRF-TOKEN",csrf)
                .method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
        return browser.send(request,HttpResponse.BodyHandlers.ofString());
    }
    @Test
    void deactivationReviewRequiresPermissionAndCurrentVersionAndRevokesAccess() throws Exception {
        var grant = authorizationCodeGrant();
        var owner = grant.browser();
        String ownerCsrf = csrf(send(owner, "GET", "/", null, null, null).body());
        assertEquals(200, send(client(), "GET", "/api/me", null, grant.accessToken(), null).statusCode());
        assertEquals(202, workspace(owner, ownerCsrf, "POST", "/deactivation", Map.of("reason", "Please review")).statusCode());
        var admin = workspaceAdmin();
        var reviewer = login(admin);
        String reviewerCsrf = csrf(send(reviewer, "GET", "/", null, null, null).body());
        String path = "/deactivation/" + grant.user().uuid() + "/review";
        var reject = Map.of("decision", "REJECTED", "reason", "Please confirm your request", "version", 0);
        assertEquals(403, workspace(owner, ownerCsrf, "POST", path, reject).statusCode());
        assertEquals(403, send(reviewer, "POST", "/workspace/api" + path,
                json.writeValueAsString(reject), null, "application/json").statusCode());
        assertEquals(400, workspace(reviewer, reviewerCsrf, "POST", path,
                Map.of("decision", "REJECTED", "reason", " ", "version", 0)).statusCode());
        assertEquals(400, workspace(reviewer, reviewerCsrf, "POST", path,
                Map.of("decision", "REJECTED", "reason", "Missing version")).statusCode());
        assertEquals(409, workspace(reviewer, reviewerCsrf, "POST", path,
                Map.of("decision", "REJECTED", "reason", "Stale version", "version", 99)).statusCode());
        assertEquals(204, workspace(reviewer, reviewerCsrf, "POST", path, reject).statusCode());
        assertEquals(409, workspace(reviewer, reviewerCsrf, "POST", path, reject).statusCode());
        var state = tree(workspace(owner, ownerCsrf, "GET", "/deactivation/mine", null));
        assertEquals("REJECTED", state.path("status").asText());
        assertEquals("Please confirm your request", state.path("review_note").asText());
        assertFalse(state.path("pending").asBoolean());
        assertEquals(200, send(client(), "GET", "/api/me", null, grant.accessToken(), null).statusCode());
        assertEquals(202, workspace(owner, ownerCsrf, "POST", "/deactivation", Map.of("reason", "Confirmed")).statusCode());
        state = tree(workspace(owner, ownerCsrf, "GET", "/deactivation/mine", null));
        assertEquals("PENDING", state.path("status").asText());
        assertTrue(state.path("review_note").isNull());
        assertEquals(204, workspace(reviewer, reviewerCsrf, "POST", path,
                Map.of("decision", "APPROVED", "reason", "Owner confirmed", "version", state.path("version").asLong())).statusCode());
        assertFalse(bean(kh.edu.iam.application.user.usecase.GetUserUseCase.class).execute(grant.user().uuid()).enabled());
        assertEquals(401, workspace(owner, ownerCsrf, "GET", "/overview", null).statusCode());
        assertEquals(401, send(client(), "GET", "/api/me", null, grant.accessToken(), null).statusCode());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM oauth2_authorization WHERE principal_name = ?",
                Integer.class, grant.user().username()));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM iam_activity WHERE subject = ? AND event = 'deactivation_approved' AND target = ?",
                Integer.class, admin.uuid().toString(), "/users/" + grant.user().uuid()));
        assertEquals(202, workspace(reviewer, reviewerCsrf, "POST", "/deactivation", Map.of("reason", "Own request")).statusCode());
        assertEquals(403, workspace(reviewer, reviewerCsrf, "POST", "/deactivation/" + admin.uuid() + "/review", reject).statusCode());
    }

    @Test
    void activityFiltersAreBoundedAndCannotExposeOtherAccounts() throws Exception {
        var user = register();
        var browser = login(user);
        String csrf = csrf(send(browser, "GET", "/", null, null, null).body());
        var admin = login(workspaceAdmin());
        String adminCsrf = csrf(send(admin, "GET", "/", null, null, null).body());
        for (int attempt = 0; attempt < 5; attempt++) {
            jdbc.update("INSERT INTO iam_activity(subject, event, success, target) VALUES (?, 'login', FALSE, '/login')", user.uuid().toString());
        }
        String query = "/activity?event=login&success=false&suspicious=true&target=%2Flogin&actor=" + user.uuid();
        var suspicious = workspace(admin, adminCsrf, "GET", query + "&audit=true", null);
        assertEquals(200, suspicious.statusCode(), suspicious.body());
        assertEquals(1, tree(suspicious).path("content").size());
        assertTrue(tree(suspicious).path("content").get(0).path("suspicious").asBoolean());
        assertEquals(0, tree(workspace(browser, csrf, "GET", "/activity?actor=" + UUID.randomUUID(), null)).path("content").size());
        assertEquals(403, workspace(browser, csrf, "GET", query + "&audit=true", null).statusCode());
        assertEquals(400, workspace(admin, adminCsrf, "GET", "/activity?audit=true&from=2026-02-02&to=2026-01-01", null).statusCode());
        assertEquals(400, workspace(admin, adminCsrf, "GET", "/activity?audit=true&actor=invalid", null).statusCode());
        assertEquals(400, workspace(admin, adminCsrf, "GET", "/activity?page=2147483647", null).statusCode());
        assertEquals(0, tree(workspace(admin, adminCsrf, "GET", "/activity?audit=true&event=%25", null)).path("content").size());
        assertEquals(0, tree(workspace(admin, adminCsrf, "GET", "/activity?audit=true&to=2000-01-01", null)).path("content").size());
    }

    @Test void workspaceRequiresSessionCsrfFreshPermissionsAndSessionOwnership() throws Exception {
        assertEquals(401,send(client(),"GET","/workspace/api/overview",null,null,null).statusCode());
        var owner=register();var browser=login(owner);
        String csrf=csrf(send(browser,"GET","/",null,null,null).body());
        assertEquals(403,workspace(browser,csrf,"GET","/users",null).statusCode());
        assertEquals(403,workspace(browser,csrf,"GET","/policy",null).statusCode());
        assertEquals(403,workspace(browser,csrf,"GET","/activity?audit=true",null).statusCode());
        var other=register();
        assertEquals(403,workspace(browser,csrf,"GET","/sessions?user="+other.uuid(),null).statusCode());
        assertEquals(403,send(browser,"POST","/workspace/api/deactivation","{\"reason\":\"test\"}",null,"application/json").statusCode());
        assertEquals(202,workspace(browser,csrf,"POST","/deactivation",Map.of("reason","Requested by account owner")).statusCode());
        var sessionRows=tree(workspace(browser,csrf,"GET","/sessions",null));
        assertTrue(sessionRows.isArray());assertFalse(sessionRows.isEmpty());
        assertEquals(64,sessionRows.get(0).get("id").asText().length());
        assertTrue(tree(workspace(browser,csrf,"GET","/activity?event=login",null)).get("content").size()>0);
        var admin=workspaceAdmin();var adminBrowser=login(admin);
        String adminCsrf=csrf(send(adminBrowser,"GET","/",null,null,null).body());
        assertEquals(200,workspace(adminBrowser,adminCsrf,"GET","/users",null).statusCode());
        jdbc.update("UPDATE user_roles SET revoked_at=CURRENT_TIMESTAMP,revoked_by='test' WHERE user_id=(SELECT id FROM users WHERE uuid=?) AND role_id=(SELECT id FROM roles WHERE name='ADMIN')",admin.uuid());
        assertEquals(403,workspace(adminBrowser,adminCsrf,"GET","/users",null).statusCode());
        assertEquals(204,workspace(browser,csrf,"DELETE","/sessions/all",null).statusCode());
        assertEquals(401,workspace(browser,csrf,"GET","/overview",null).statusCode());
    }
    @Test void workspaceClientCrudPoliciesAndUsernameUpdatesAreIntegrated() throws Exception {
        var admin=workspaceAdmin();var browser=login(admin);
        String csrf=csrf(send(browser,"GET","/",null,null,null).body());
        String clientId=unique();
        var input=new LinkedHashMap<String,Object>();
        input.put("clientId",clientId);input.put("clientName","Workspace test client");input.put("confidential",true);
        input.put("clientSecret","workspace-secret-1234567890");input.put("grantTypes",List.of("authorization_code","refresh_token"));
        input.put("redirectUris",List.of("https://example.com/callback"));input.put("scopes",List.of("openid","profile"));
        var created=workspace(browser,csrf,"POST","/clients",input);
        assertEquals(201,created.statusCode(),created.body());assertFalse(created.body().contains("workspace-secret"));assertFalse(created.body().contains("$2"));
        String id=tree(created).get("uuid").asText();
        input.put("clientSecret","");input.put("clientName","Updated workspace client");
        assertEquals(200,workspace(browser,csrf,"PUT","/clients/"+id,input).statusCode());
        input.put("redirectUris",List.of("https://example.com/callback#fragment"));
        assertEquals(400,workspace(browser,csrf,"PUT","/clients/"+id,input).statusCode());
        assertEquals(204,workspace(browser,csrf,"DELETE","/clients/"+id,null).statusCode());
        assertEquals(404,workspace(browser,csrf,"GET","/clients/"+id,null).statusCode());
        var oldPolicy=tree(workspace(browser,csrf,"GET","/policy",null));
        var policy=new LinkedHashMap<String,Object>();policy.put("sessionMinutes",45);policy.put("accessMinutes",7);policy.put("refreshMinutes",1500);
        policy.put("passwordMinimum",16);policy.put("requireNumber",true);policy.put("requireUppercase",true);policy.put("requireConsent",true);policy.put("version",oldPolicy.get("version").asLong());
        try {
            assertEquals(200,workspace(browser,csrf,"PUT","/policy",policy).statusCode());
            assertEquals(409,workspace(browser,csrf,"PUT","/policy",policy).statusCode());
            assertThrows(kh.edu.iam.application.exception.ApplicationException.class,()->bean(kh.edu.iam.application.port.out.PasswordHasher.class).hash("Longpassword!"));
        } finally {
            jdbc.update("UPDATE iam_workspace_policy SET session_minutes=30,access_minutes=5,refresh_minutes=1440,password_minimum=8,require_number=FALSE,require_uppercase=FALSE,require_consent=TRUE,version=version+1 WHERE id=1");
        }
        var user=register();String renamed=unique();
        var update=workspace(browser,csrf,"PATCH","/users/"+user.uuid(),Map.of("username",renamed,"version",user.version()));
        assertEquals(200,update.statusCode(),update.body());assertEquals(renamed,tree(update).get("username").asText());
        assertEquals(409,workspace(browser,csrf,"PATCH","/users/"+user.uuid(),Map.of("username",unique(),"version",user.version())).statusCode());
        assertNotNull(login(bean(kh.edu.iam.application.user.usecase.GetUserUseCase.class).execute(user.uuid())));
    }
    @Test void accountSessionRevocationAndOwnRequestState() throws Exception {
        var user = register();
        var first = login(user); var second = login(user);
        String token = csrf(send(first,"GET","/",null,null,null).body());
        String secondToken = csrf(send(second,"GET","/",null,null,null).body());
        var secondRows = tree(workspace(second,secondToken,"GET","/sessions",null));
        String secondKey = null;
        for (var row : secondRows) {
            assertEquals("local", row.path("location").path("status").asText());
            if (row.path("current").asBoolean()) secondKey = row.path("id").asText();
        }
        assertNotNull(secondKey);
        assertEquals(204,workspace(first,token,"DELETE","/sessions/"+secondKey,null).statusCode());
        assertEquals(401,workspace(second,secondToken,"GET","/overview",null).statusCode());
        assertEquals(200,workspace(first,token,"GET","/overview",null).statusCode());
        assertFalse(tree(workspace(first,token,"GET","/deactivation/mine",null)).path("pending").asBoolean());
        assertEquals(202,workspace(first,token,"POST","/deactivation",Map.of("reason","Owner requested review")).statusCode());
        var state = tree(workspace(first,token,"GET","/deactivation/mine",null));
        assertTrue(state.path("pending").asBoolean());
        assertEquals("Owner requested review",state.path("reason").asText());
        assertEquals(202,workspace(first,token,"POST","/deactivation",Map.of("reason","Updated request")).statusCode());
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM iam_deactivation_request WHERE subject=?",Integer.class,user.uuid()));
        assertTrue(bean(kh.edu.iam.application.user.usecase.GetUserUseCase.class).execute(user.uuid()).enabled());
        var other = login(register());
        String otherToken=csrf(send(other,"GET","/",null,null,null).body());
        assertFalse(tree(workspace(other,otherToken,"GET","/deactivation/mine",null)).path("pending").asBoolean());
        assertEquals(403,workspace(other,otherToken,"GET","/deactivation",null).statusCode());
        assertEquals(403,workspace(other,otherToken,"DELETE","/sessions/"+secondKey+"?user="+user.uuid(),null).statusCode());
        var third=login(user);String thirdToken=csrf(send(third,"GET","/",null,null,null).body());
        assertEquals(204,workspace(first,token,"DELETE","/sessions/all",null).statusCode());
        assertEquals(401,workspace(third,thirdToken,"GET","/overview",null).statusCode());
        assertEquals(401,workspace(first,token,"GET","/overview",null).statusCode());
    }

    @Test void profileDuplicatesHaveFieldErrorsAndLoginHistoryIncludesFailures() throws Exception {
        var user=register();var taken=register();var browser=login(user);
        var edit=send(browser,"GET","/profile/edit",null,null,null);
        String csrf=csrf(edit.body());
        var input=new LinkedHashMap<String,String>();
        input.put("_csrf",csrf); input.put("version",json.readTree(reactPayload(edit.body())).path("data").path("values").path("version").asText());
        input.put("username",taken.username());input.put("email",user.email());
        input.put("givenName","Dara");input.put("familyName","Sok");input.put("locale","en");
        var duplicate=send(browser,"POST","/profile/edit",form(input),null,"application/x-www-form-urlencoded");
        assertEquals(400,duplicate.statusCode());
        assertEquals("validation.duplicate.username",json.readTree(reactPayload(duplicate.body())).path("data").path("errors").path("username").asText());
        input.put("username",user.username());input.put("email",taken.email());
        duplicate=send(browser,"POST","/profile/edit",form(input),null,"application/x-www-form-urlencoded");
        assertEquals(400,duplicate.statusCode());
        assertEquals("validation.duplicate.email",json.readTree(reactPayload(duplicate.body())).path("data").path("errors").path("email").asText());
        var anonymous=client();String loginCsrf=csrf(send(anonymous,"GET","/login",null,null,null).body());
        send(anonymous,"POST","/login",form(Map.of("_csrf",loginCsrf,"username",user.username(),"password","incorrect")),null,"application/x-www-form-urlencoded");
        assertFalse(tree(workspace(browser,csrf,"GET","/activity?event=login&success=false",null)).path("content").isEmpty());
        assertFalse(tree(workspace(browser,csrf,"GET","/activity?event=profile_updated&success=false",null)).path("content").isEmpty());
    }

    @Test
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named="iam.browser.checks",matches="true")
    void workspaceBrowserManagementAndAccountFlows() throws Exception {
        var admin=workspaceAdmin();var user=register();
        var root=java.nio.file.Path.of(System.getProperty("iam.project.root"));
        var process=new ProcessBuilder("node",root.resolve("scripts/workspace-check.mjs").toString()).directory(root.toFile()).redirectErrorStream(true);
        process.environment().put("IAM_BROWSER_URL",origin());process.environment().put("IAM_BROWSER_USERNAME",admin.username());
        process.environment().put("IAM_BROWSER_USER",user.username());process.environment().put("IAM_BROWSER_PASSWORD",PASSWORD);
        process.environment().put("IAM_BROWSER_ADMIN_UUID", admin.uuid().toString());
        var log=root.resolve("verification/workspace-browser.log");
        var running=process.redirectOutput(log.toFile()).start();
        if(!running.waitFor(120,java.util.concurrent.TimeUnit.SECONDS)){running.destroyForcibly();fail("Workspace browser timeout");}
        assertEquals(0,running.exitValue(),java.nio.file.Files.readString(log));
    }

}
