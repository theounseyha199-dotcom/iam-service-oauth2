package co.istad.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.session.SessionRegistryImpl;

import kh.edu.iam.infrastructure.security.IamUserPrincipal;

class IamUserPrincipalTest {
    @Test
    void deserializedAuthorizationPrincipalFindsTheOriginalBrowserSession() {
        UUID subject = UUID.randomUUID();
        var browser = principal(subject, "original-name");
        var restored = principal(subject, "updated-name");
        var registry = new SessionRegistryImpl();
        registry.registerNewSession("browser-session", browser);

        assertEquals(browser, restored);
        assertEquals(subject.toString(), browser.getUsername());
        assertEquals(browser.getUsername(), restored.getUsername());
        assertEquals("updated-name", restored.loginName());
        assertEquals(browser.hashCode(), restored.hashCode());
        assertEquals("browser-session", registry.getAllSessions(restored, false).getFirst().getSessionId());
        assertTrue(registry.getAllSessions(principal(UUID.randomUUID(), "original-name"), false).isEmpty());
    }

    private IamUserPrincipal principal(UUID subject, String username) {
        return new IamUserPrincipal(subject, username, Set.of("ROLE_USER"), true, true, true, true);
    }
}
