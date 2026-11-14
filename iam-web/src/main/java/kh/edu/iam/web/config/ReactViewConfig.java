package kh.edu.iam.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kh.edu.iam.application.account.result.ConnectedAppResult;
import kh.edu.iam.application.identity.result.IdentityResult;
import kh.edu.iam.application.user.result.UserAuthoritiesResult;
import kh.edu.iam.application.user.result.UserResult;
import kh.edu.iam.web.view.BrowserPageAdvice;
import kh.edu.iam.web.view.BrowserUser;
import kh.edu.iam.application.user.usecase.GetUserAuthoritiesUseCase;
import kh.edu.iam.web.view.Page;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** React renders all browser pages; existing controllers retain form and protocol handling. */
@Configuration
public class ReactViewConfig {
    public static final Set<String> PAGES = Set.of("login", "register", "index", "profile-edit",
            "change-password", "linked-accounts", "unlink-account", "connected-apps", "revoke-app",
            "consent", "logout", "federation-complete", "forgot-password", "reset-password",
            "registration-pending", "verify-email", "error");
    private static final Set<String> SAFE_MODEL_KEYS = Set.of("claims", "values", "errors", "fieldErrors",
            "errorCode", "error", "saved", "linked", "unlinked", "failed", "revoked", "sent", "done",
            "email", "givenName", "familyName", "needEmail", "needGivenName", "needFamilyName",
            "flow", "clientId", "state", "loginError", "loggedOut", "oauth2Error", "registered", "status");

    @Bean
    public ViewResolver reactViewResolver(BrowserPageAdvice advice, JsonMapper json,
            BrowserUser current, GetUserAuthoritiesUseCase authorities) throws java.io.IOException {
        // Read Vite's generated entry: asset hashes always match the packaged build.
        String entry = new ClassPathResource("static/react/index.html").getContentAsString(StandardCharsets.UTF_8);
        var keys = PropertiesLoaderUtils.loadAllProperties("messages.properties").stringPropertyNames();
        return new ReactResolver(entry, keys, advice, json, current, authorities);
    }

    @Bean
    public org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver reactErrorViewResolver() {
        return (request, status, model) -> {
            var result = new org.springframework.web.servlet.ModelAndView("error");
            result.addObject("status", status.value());
            result.setStatus(status);
            return result;
        };
    }

    private record ReactResolver(String entry, Set<String> keys, BrowserPageAdvice advice, JsonMapper json,
            BrowserUser current, GetUserAuthoritiesUseCase authorities)
            implements ViewResolver, Ordered {
        @Override public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
        @Override public View resolveViewName(String name, java.util.Locale locale) {
            if (!PAGES.contains(name)) return null; // Redirect/forward views remain Spring's responsibility.
            return new View() {
                @Override public String getContentType() { return "text/html;charset=UTF-8"; }
                @Override public void render(Map<String, ?> model, HttpServletRequest request,
                        HttpServletResponse response) throws Exception {
                    Page p = advice.page(request, RequestContextUtils.getLocale(request));
                    var messages = new LinkedHashMap<String, String>();
                    for (String key : keys) messages.put(key, p.t(key));
                    var data = new LinkedHashMap<String, Object>();
                    for (String key : SAFE_MODEL_KEYS) if (model.containsKey(key)) data.put(key, model.get(key));
                    // Project only display fields; never serialize principals, entities or the entire MVC model.
                    // All authenticated account routes share the same first-render shell data.
                    if (Set.of("index", "profile-edit", "change-password", "linked-accounts",
                            "unlink-account", "connected-apps", "revoke-app", "consent").contains(name)) {
                        var viewer = model.get("user") instanceof UserResult user ? user : current.current();
                        data.putIfAbsent("claims", Map.of("subject", viewer.uuid().toString(),
                                "username", viewer.username(), "email", viewer.email(),
                                "givenName", viewer.profile().givenName(), "familyName", viewer.profile().familyName(),
                                "zoneinfo", viewer.profile().zoneinfo() == null ? "" : viewer.profile().zoneinfo(),
                                "phoneNumber", viewer.profile().phoneNumber() == null ? "" : viewer.profile().phoneNumber(),
                                "pictureUrl", viewer.profile().pictureUrl() == null ? "" : viewer.profile().pictureUrl()));
                        var access = model.get("authorities") instanceof UserAuthoritiesResult a
                                ? a : authorities.execute(viewer.uuid());
                        data.put("roles", access.roles().stream().map(r -> r.name()).toList());
                        data.put("permissions", access.effectivePermissions().stream().sorted().toList());
                    }
                    if (model.get("user") instanceof UserResult user) {
                        data.put("emailVerified", user.emailVerified());
                        data.put("subject", user.uuid());
                        data.put("phoneVerified", user.profile().phoneNumberVerified());
                    }
                    if (model.get("authorities") instanceof UserAuthoritiesResult a) {
                        data.put("roles", a.roles().stream().map(r -> r.name()).toList());
                        data.put("permissions", a.effectivePermissions().stream().sorted().toList());
                    }
                    if (model.get("identities") instanceof List<?> identities) {
                        data.put("identities", identities.stream().map(i -> identity((IdentityResult)i)).toList());
                    }
                    if (model.get("identity") instanceof IdentityResult i) data.put("identity", identity(i));
                    if (model.get("apps") instanceof List<?> apps) data.put("apps", apps);
                    for (String key : List.of("app", "client")) {
                        if (model.get(key) instanceof ConnectedAppResult app) data.put(key, app);
                    }
                    var bootstrap = Map.of("page", name, "data", data, "messages", messages,
                            "language", p.language(), "languagePath", p.languagePath(),
                            "contextPath", p.contextPath(), "csrfName", p.csrfName(),
                            "csrfToken", p.csrfToken(), "providers", p.providers());
                    String payload = json.writeValueAsString(bootstrap)
                            .replace("&", "\\u0026").replace("<", "\\u003c").replace(">", "\\u003e")
                            .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
                    String html = entry.replace("<html lang=\"en\">", "<html lang=\"" + p.language() + "\">")
                            .replace("<div id=\"root\"></div>", "<script id=\"iam-page\" type=\"application/json\">"
                                    + payload + "</script><div id=\"root\"></div>");
                    if (!p.contextPath().isEmpty()) html = html.replace("\"/react/", "\"" + p.contextPath() + "/react/");
                    response.setContentType(getContentType());
                    response.setHeader("Cache-Control", "no-store");
                    response.setHeader("Referrer-Policy", "no-referrer");
                    response.getWriter().write(html);
                }
            };
        }
        private static Map<String, String> identity(IdentityResult i) {
            return Map.of("uuid", i.uuid().toString(), "provider", i.provider().name().toLowerCase(java.util.Locale.ROOT),
                    "displayName", i.displayName() == null ? "" : i.displayName());
        }
    }
}
