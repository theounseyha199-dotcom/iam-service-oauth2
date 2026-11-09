package kh.edu.iam.infrastructure.security;

import kh.edu.iam.application.user.usecase.RecordLoginAttemptUseCase;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class LoginAttemptListener {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginAttemptListener.class);
    private final RecordLoginAttemptUseCase attempts;

    @EventListener
    public void success(AuthenticationSuccessEvent event) {
        if (event.getAuthentication().getPrincipal() instanceof IamUserPrincipal principal) {
            record(principal.getUsername(), true);
        }
    }

    @EventListener
    public void failure(AbstractAuthenticationFailureEvent event) {
        if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            record(event.getAuthentication().getName(), false);
        }
    }

    private void record(String username, boolean success) {
        try {
            attempts.execute(username, success);
        } catch (RuntimeException ex) {
            LOG.warn("Unable to persist a login attempt ({})", ex.getClass().getSimpleName());
        }
    }
}
