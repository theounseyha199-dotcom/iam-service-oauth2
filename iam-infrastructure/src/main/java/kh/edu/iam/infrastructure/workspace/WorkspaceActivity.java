package kh.edu.iam.infrastructure.workspace;

import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.infrastructure.security.IamUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Bounded metadata only: no request bodies, query strings, cookies or credentials. */
@Component
@RequiredArgsConstructor
public class WorkspaceActivity implements WebMvcConfigurer, HandlerInterceptor {
    private final JdbcTemplate jdbc;
    private final CurrentActorProvider actors;
    private final UserRepository users;
    private final SessionRegistry sessions;

    public static String key(String sessionId) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(sessionId.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }

    @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(this); }

    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var session = request.getSession(false);
        if (session != null && sessions.getSessionInformation(session.getId()) != null) {
            int minutes = jdbc.queryForObject("SELECT session_minutes FROM iam_workspace_policy WHERE id=1", Integer.class);
            session.setMaxInactiveInterval(minutes * 60);
            jdbc.update("INSERT INTO iam_session_metadata(session_key,ip,device) VALUES (?,?,?) ON CONFLICT DO NOTHING",
                    key(session.getId()), trim(request.getRemoteAddr(),100), trim(request.getHeader("User-Agent"),512));
        }
        return true;
    }

    @Override public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!java.util.Set.of("GET","HEAD","OPTIONS").contains(request.getMethod())
                && (path.startsWith("/api/") || path.startsWith("/workspace/api/") || path.startsWith("/profile/"))) {
            String location = response.getHeader("Location");
            boolean success = response.getStatus() < 400 && (location == null || !location.matches(".*[?&](error|failed)([=&].*)?$"));
            String action = path.equals("/profile/edit") ? "profile_updated"
                    : path.equals("/profile/password") ? "password_changed"
                    : path.matches("/profile/accounts/[^/]+/unlink") ? "social_unlinked"
                    : path.contains("/accounts/link/") ? "social_link_started"
                    : path.startsWith("/workspace/api/sessions/") || path.startsWith("/api/console/sessions/") ? "session_ended"
                    : path.endsWith("/deactivation") ? "deactivation_requested"
                    : request.getMethod()+" "+path;
            record(actors.currentActor(), action, success, request);
        }
    }

    @EventListener public void security(AccountSecurityEvent event) {
        record(event.subject().toString(), event.action(), true, request());
    }
    @EventListener public void destroyed(org.springframework.security.web.session.HttpSessionDestroyedEvent event) {
        jdbc.update("DELETE FROM iam_session_metadata WHERE session_key=?",key(event.getSession().getId()));
    }
    @EventListener public void login(AuthenticationSuccessEvent event) {
        if (event.getAuthentication().getPrincipal() instanceof IamUserPrincipal p)
            record(p.uuid().toString(), "login", true, request());
    }
    @EventListener public void failed(AbstractAuthenticationFailureEvent event) {
        users.findByUsernameOrEmail(event.getAuthentication().getName()).ifPresent(user ->
                record(user.getUuid().toString(), "login", false, request()));
    }
    private HttpServletRequest request() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a ? a.getRequest() : null;
    }
    private void record(String subject, String event, boolean success, HttpServletRequest request) {
        try {
            jdbc.update("INSERT INTO iam_activity(subject,event,success,ip,device,target) VALUES (?,?,?,?,?,?)",
                    trim(subject,100),trim(event,200),success,request==null?null:trim(request.getRemoteAddr(),100),
                    request==null?null:trim(request.getHeader("User-Agent"),512),
                    request==null?null:trim(request.getRequestURI().substring(request.getContextPath().length()),200));
        } catch (org.springframework.dao.DataAccessException ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Activity recording failed ({})",ex.getClass().getSimpleName());
        }
    }
    private static String trim(String value,int max) { return value==null?"":value.substring(0,Math.min(value.length(),max)); }
}
