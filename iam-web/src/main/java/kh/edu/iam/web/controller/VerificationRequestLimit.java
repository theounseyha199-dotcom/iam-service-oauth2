package kh.edu.iam.web.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Per-instance backstop; deployments should also limit requests at ingress. */
@Component
@Order(1)
public class VerificationRequestLimit extends OncePerRequestFilter {
    private record Window(long startedAt, int count) {}
    private final Map<String, Window> clients = new HashMap<>();
    private static final Set<String> PATHS = Set.of("/register", "/api/auth/register", "/resend-verification", "/verify-email");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.getMethod().equals("POST") && PATHS.contains(request.getServletPath())
                && !allow(request.getRemoteAddr(), System.currentTimeMillis())) {
            response.setHeader("Retry-After", "60");
            response.sendError(429);
            return;
        }
        chain.doFilter(request, response);
    }

    private synchronized boolean allow(String address, long now) {
        clients.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= 60000);
        var previous = clients.get(address);
        if (previous == null && clients.size() >= 10000) return false;
        if (previous != null && previous.count() >= 20) return false;
        clients.put(address, new Window(previous == null ? now : previous.startedAt(), previous == null ? 1 : previous.count()+1));
        return true;
    }
}
