package kh.edu.iam.infrastructure.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        http.oauth2AuthorizationServer(
                        (authorizationServer) -> {
                            http.securityMatcher(authorizationServer.getEndpointsMatcher());

                            // Enable OpenID Connect 1.0
                            authorizationServer.authorizationEndpoint(
                                    endpoint -> endpoint.consentPage("/oauth2/consent"));
                            authorizationServer.oidc(
                                    oidc ->
                                            oidc.logoutEndpoint(logout -> logout.errorResponseHandler((request, response, exception) -> {
                                                        var error = ((org.springframework.security.oauth2.core.OAuth2AuthenticationException) exception).getError();
                                                        String description = error.getDescription();
                                                        String parameter = description != null && description.matches("OpenID Connect 1[.]0 Logout Request Parameter: [a-z_]+")
                                                                ? description.substring(description.lastIndexOf(' ') + 1) : "unknown";
                                                        org.slf4j.LoggerFactory.getLogger(SecurityConfig.class).warn(
                                                                "OIDC logout rejected: code={}, parameter={}", error.getErrorCode(), parameter);
                                                        response.sendError(400);
                                                    }))
                                                    .clientRegistrationEndpoint(
                                                    registration ->
                                                            registration.authenticationProviders(
                                                                    ClientRegistrationPolicy::configure)));
                        })
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
                .exceptionHandling(
                        (exceptions) ->
                                exceptions
                                        .defaultAuthenticationEntryPointFor(
                                                new BearerTokenAuthenticationEntryPoint(),
                                                new OrRequestMatcher(
                                                        PathPatternRequestMatcher.withDefaults()
                                                                .matcher("/connect/register"),
                                                        PathPatternRequestMatcher.withDefaults().matcher("/userinfo")))
                                        .defaultAuthenticationEntryPointFor(
                                                new LoginUrlAuthenticationEntryPoint("/login"),
                                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            OAuth2AuthorizationRequestResolver resolver,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> requests,
            @Qualifier("federationLoginHandler") AuthenticationSuccessHandler federationSuccess,
            @Qualifier("federationLoginHandler") AuthenticationFailureHandler federationFailure)
            throws Exception {
        http.authorizeHttpRequests(
                        (authorize) ->
                                authorize
                                        .requestMatchers(
                                                "/login",
                                                "/register",
                                                "/error",
                                                "/federation/complete",
                                                "/federation/cancel",
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/scalar",
                                                "/scalar/**",
                                                "/css/**",
                                                "/images/**",
                                                "/js/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                .exceptionHandling(
                        errors -> errors.accessDeniedHandler((request, response, exception) -> response.sendError(403)))
                .formLogin(form -> form.loginPage("/login").permitAll())
                .oauth2Login(
                        oauth2 ->
                                oauth2.loginPage("/login")
                                        .authorizationEndpoint(
                                                endpoint ->
                                                        endpoint.authorizationRequestResolver(resolver)
                                                                .authorizationRequestRepository(requests))
                                        .successHandler(federationSuccess)
                                        .failureHandler(federationFailure)
                                        .permitAll());

        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public JdbcOAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate, RegisteredClientRepository clients) {
        return JdbcAuthorizationPersistence.create(jdbcTemplate, clients);
    }

    @Bean
    public JdbcOAuth2AuthorizationConsentService authorizationConsentService(
            JdbcTemplate jdbcTemplate, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, clients);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(@Value("${app.iam.issuer}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }
}
