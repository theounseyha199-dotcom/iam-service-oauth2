package kh.edu.iam.infrastructure.security;

import kh.edu.iam.application.port.out.*;
import kh.edu.iam.application.common.PasswordPolicy;
import kh.edu.iam.application.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.Clock;
import java.sql.Timestamp;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class EmailPasswordRecovery implements PasswordRecovery {
    private final UserRepository users;
    private final PasswordHasher passwords;
    private final TransactionRunner transactions;
    private final JdbcTemplate jdbc;
    private final ObjectProvider<JavaMailSender> mail;
    private final SessionRegistry sessions;
    private final Clock clock;
    private final String issuer;
    private final String from;
    private final org.springframework.context.ApplicationEventPublisher events;
    private final SecureRandom random = new SecureRandom();

    public EmailPasswordRecovery(UserRepository users, PasswordHasher passwords, TransactionRunner transactions,
            JdbcTemplate jdbc, ObjectProvider<JavaMailSender> mail, SessionRegistry sessions, Clock clock,
            @Value("${app.iam.issuer}") String issuer,
            @Value("${app.iam.mail-from:no-reply@localhost}") String from, org.springframework.context.ApplicationEventPublisher events) {
        this.users=users; this.passwords=passwords; this.transactions=transactions; this.jdbc=jdbc;
        this.mail=mail; this.sessions=sessions; this.clock=clock; this.issuer=issuer; this.from=from; this.events=events;
    }

    @Override
    public void request(String email) {
        // Check infrastructure before lookup so delivery failures cannot disclose account existence.
        var sender = mail.getIfAvailable();
        if (sender == null) throw new ApplicationException("mail_unavailable", "Email delivery is unavailable");
        transactions.required(() -> {
            var found = users.lockByUsernameOrEmail(email);
            if (found.isEmpty()) return null;
            var user = found.get();
            if (!user.getEmail().equals(email) || !user.hasPassword() || !user.isEnabled()
                    || user.getDeletion().deleted()) return null;
            var now = clock.instant();
            var recent = jdbc.queryForObject("SELECT count(*) FROM password_reset_tokens WHERE user_uuid = ? AND requested_at > ?",
                    Integer.class, user.getUuid(), Timestamp.from(now.minusSeconds(60)));
            if (recent != null && recent > 0) return null;
            byte[] bytes = new byte[32]; random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            jdbc.update("DELETE FROM password_reset_tokens WHERE user_uuid = ?", user.getUuid());
            jdbc.update("INSERT INTO password_reset_tokens(user_uuid, token_hash, email, expires_at, requested_at) VALUES (?, ?, ?, ?, ?)",
                    user.getUuid(), hash(token), email, Timestamp.from(now.plusSeconds(900)), Timestamp.from(now));
            var message = new SimpleMailMessage();
            message.setFrom(from); message.setTo(email); message.setSubject("Reset your ISTAD Identity password");
            message.setText("Use this link to set a new password. It expires in 15 minutes and works once.\n\n"
                    + issuer.replaceAll("/+$", "") + "/reset-password?token=" + token
                    + "\n\nIf you did not request this, you can ignore this email.");
            sender.send(message);
            return null;
        });
    }

    @Override
    public void reset(String token, String password, String confirmation) {
        PasswordPolicy.validate(password, confirmation);
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw invalid();
        String username = transactions.required(() -> {
            var ids = jdbc.query("SELECT user_uuid FROM password_reset_tokens WHERE token_hash = ?",
                    (rs, n) -> rs.getObject(1, UUID.class), hash(token));
            if (ids.isEmpty()) throw invalid();
            // Match request's lock order. Serializes resets, replacement requests and password changes.
            var user = users.lockByUuid(ids.getFirst()).orElseThrow(EmailPasswordRecovery::invalid);
            int consumed = jdbc.update("DELETE FROM password_reset_tokens WHERE user_uuid = ? AND token_hash = ? AND email = ? AND expires_at > ? AND requested_at >= (SELECT COALESCE(password_changed_at, created_at) FROM users WHERE uuid = ?)",
                    user.getUuid(), hash(token), user.getEmail(), Timestamp.from(clock.instant()), user.getUuid());
            if (consumed != 1 || !user.hasPassword() || !user.isEnabled() || user.getDeletion().deleted()) throw invalid();
            user.replacePasswordHash(passwords.hash(password), user.getUsername(), clock.instant());
            users.save(user);
            jdbc.update("DELETE FROM oauth2_authorization WHERE principal_name = ?", user.getUsername());
            return user.getUsername();
        });
        for (Object principal : sessions.getAllPrincipals()) {
            if (principal instanceof UserDetails details && details.getUsername().equals(username)) {
                sessions.getAllSessions(principal, false).forEach(session -> session.expireNow());
            }
        }
        users.findByUsernameOrEmail(username).ifPresent(user -> events.publishEvent(
                new kh.edu.iam.infrastructure.workspace.AccountSecurityEvent(user.getUuid(), "password_reset")));
        // Delivery failure must never turn a committed reset into a failed response.
        try {
            var sender = mail.getIfAvailable();
            if (sender != null) {
                var user = users.findByUsernameOrEmail(username).orElseThrow();
                var message = new SimpleMailMessage();
                message.setFrom(from); message.setTo(user.getEmail());
                message.setSubject("Your ISTAD Identity password was changed");
                message.setText("Your password has been reset. If you did not make this change, contact your administrator immediately.");
                sender.send(message);
            }
        } catch (RuntimeException ex) {
            org.slf4j.LoggerFactory.getLogger(EmailPasswordRecovery.class)
                    .warn("Password change notification failed ({})", ex.getClass().getSimpleName());
        }
    }

    private static ApplicationException invalid() {
        return new ApplicationException("invalid_reset_token", "Reset link is invalid or expired");
    }
    static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
