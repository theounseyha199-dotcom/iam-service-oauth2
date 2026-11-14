package kh.edu.iam.restapi.security;

import kh.edu.iam.application.port.out.EmailVerification;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stateless bearer authentication is scoped to /api; browser sessions retain CSRF protection.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class ApiSecurityConfig {
    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http, JwtDecoder jwtDecoder, @Value("${app.iam.issuer}") String issuer,
            EmailVerification verification,
            kh.edu.iam.application.user.usecase.GetUserUseCase users) throws Exception {
        var validator = JwtValidators.createDefaultWithIssuer(issuer);
        JwtDecoder ownIssuerDecoder =
                token -> {
                    var jwt = jwtDecoder.decode(token); // Verify with this authorization server's persistent key.
                    var result = validator.validate(jwt);
                    if (result.hasErrors()) {
                        throw new JwtValidationException("Invalid IAM bearer token", result.getErrors());
                    }
                    if (jwt.getExpiresAt() == null) {
                        throw new BadJwtException("A bearer token must expire");
                    }
                    // OIDC ID tokens must not be usable as API access tokens.
                    if (jwt.getClaims().containsKey("nonce") || !jwt.getClaims().containsKey("scope")) {
                        throw new BadJwtException("An access token is required");
                    }
                    java.util.UUID userId = null;
                    try {
                        userId = java.util.UUID.fromString(jwt.getSubject());
                    } catch (IllegalArgumentException ex) {
                        // Client-credentials subjects are client IDs, rather than user UUIDs.
                    }
                    if (userId != null) {
                        if (verification.isPending(userId)) {
                            throw new BadJwtException("Email verification is required");
                        }
                        try {
                            var user = users.execute(userId);
                            if (!user.enabled() || !user.accountNonLocked() || !user.accountNonExpired()
                                    || !user.credentialsNonExpired()) {
                                throw new BadJwtException("Account unavailable");
                            }
                        } catch (kh.edu.iam.application.exception.ApplicationException ex) {
                            throw new BadJwtException("Account unavailable", ex);
                        }
                    }
                    return jwt;
                };
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> {
                    var names = new LinkedHashSet<String>();
                    addStrings(names, jwt.getClaims().get("authorities"));
                    addStrings(names, jwt.getClaims().get("permissions"));
                    return names.stream()
                            .filter(name -> name.matches("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+") || name.startsWith("ROLE_"))
                            .map(SimpleGrantedAuthority::new)
                            .map(a -> (org.springframework.security.core.GrantedAuthority) a)
                            .toList();
                });
        return http.securityMatcher("/api/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.POST, "/api/auth/register")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        errors ->
                                errors.authenticationEntryPoint(
                                                (request, response, exception) -> writeProblem(response, 401))
                                        .accessDeniedHandler(
                                                (request, response, exception) -> writeProblem(response, 403)))
                .oauth2ResourceServer(
                        resource ->
                                resource.jwt(jwt -> jwt.decoder(ownIssuerDecoder).jwtAuthenticationConverter(converter))
                                        .authenticationEntryPoint(
                                                (request, response, exception) -> writeProblem(response, 401))
                                        .accessDeniedHandler(
                                                (request, response, exception) -> writeProblem(response, 403)))
                .build();
    }

    private static void addStrings(Set<String> target, Object claim) {
        if (claim instanceof Collection<?> values) {
            values.stream().filter(String.class::isInstance).map(String.class::cast).forEach(target::add);
        }
    }

    private static void writeProblem(HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        if (status == 401) {
            response.setHeader("WWW-Authenticate", "Bearer");
        }
        String name = status == 401 ? "Unauthorized" : "Forbidden";
        String code = status == 401 ? "unauthorized" : "forbidden";
        response.getWriter()
                .write(
                        "{\"type\":\"https://iam.istad.co/problems/"
                                + code
                                + "\",\"title\":\""
                                + name
                                + "\",\"status\":"
                                + status
                                + "}");
    }
}
