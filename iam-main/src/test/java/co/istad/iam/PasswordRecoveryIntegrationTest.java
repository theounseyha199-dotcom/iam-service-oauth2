package co.istad.iam;

import static org.junit.jupiter.api.Assertions.*;
import kh.edu.iam.application.port.out.PasswordRecovery;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.port.out.PasswordHasher;
import kh.edu.iam.application.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.HexFormat;
import java.util.UUID;
import java.net.URI;
import java.net.http.*;

class PasswordRecoveryIntegrationTest extends IamIntegrationTestSupport {
    private String token(UUID uuid, String email, Instant expiry) throws Exception {
        String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(UUID.randomUUID().toString().substring(0,32).getBytes(StandardCharsets.UTF_8));
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        jdbc.update("INSERT INTO password_reset_tokens(user_uuid,token_hash,email,expires_at,requested_at) VALUES (?,?,?,?,?)", uuid,hash,email,Timestamp.from(expiry),Timestamp.from(Instant.now()));
        return token;
    }

    @Test void resetWorksOnceAndRejectsExpiredAndChangedEmailTokens() throws Exception {
        var user = register();
        var recovery = bean(PasswordRecovery.class);
        var repository = bean(UserRepository.class);
        String token = token(user.uuid(),user.email(),Instant.now().plusSeconds(900));
        assertThrows(IllegalArgumentException.class, () -> recovery.reset(token,"short","short"));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM password_reset_tokens WHERE user_uuid=?",Integer.class,user.uuid()));
        recovery.reset(token,"NewPassword2026!","NewPassword2026!");
        assertTrue(bean(PasswordHasher.class).matches("NewPassword2026!",repository.findByUuid(user.uuid()).orElseThrow().getPasswordHash()));
        assertThrows(ApplicationException.class, () -> recovery.reset(token,PASSWORD,PASSWORD));
        String expired = token(user.uuid(),user.email(),Instant.now().minusSeconds(1));
        assertThrows(ApplicationException.class, () -> recovery.reset(expired,PASSWORD,PASSWORD));
        jdbc.update("DELETE FROM password_reset_tokens WHERE user_uuid=?",user.uuid());
        String wrongEmail = token(user.uuid(),"previous@example.com",Instant.now().plusSeconds(900));
        assertThrows(ApplicationException.class, () -> recovery.reset(wrongEmail,PASSWORD,PASSWORD));
    }

    @Test void emailContainsOnlyAnOpaqueSingleUseLinkAndRequestsAreThrottled() throws Exception {
        var user = register();
        var messages = new java.util.ArrayList<org.springframework.mail.SimpleMailMessage>();
        var sender = new org.springframework.mail.javamail.JavaMailSenderImpl() {
            @Override public void send(org.springframework.mail.SimpleMailMessage message) { messages.add(message); }
        };
        var factory = new org.springframework.beans.factory.support.StaticListableBeanFactory();
        factory.addBean("mail", sender);
        var sessions = bean(org.springframework.security.core.session.SessionRegistry.class);
        var recovery = new kh.edu.iam.infrastructure.security.EmailPasswordRecovery(
                bean(UserRepository.class), bean(PasswordHasher.class),
                bean(kh.edu.iam.application.port.out.TransactionRunner.class), jdbc,
                factory.getBeanProvider(org.springframework.mail.javamail.JavaMailSender.class), sessions,
                java.time.Clock.systemUTC(), "https://iam.example.com", "no-reply@example.com", context);
        recovery.request("unknown@example.com");
        assertTrue(messages.isEmpty());
        recovery.request(user.email());
        recovery.request(user.email());
        assertEquals(1,messages.size());
        assertEquals(user.email(),messages.getFirst().getTo()[0]);
        String text = messages.getFirst().getText();
        var matcher = java.util.regex.Pattern.compile("https://iam.example.com/reset-password\\?token=([A-Za-z0-9_-]{43})").matcher(text);
        assertTrue(matcher.find());
        String secret = matcher.group(1);
        assertNotEquals(secret,jdbc.queryForObject("SELECT token_hash FROM password_reset_tokens WHERE user_uuid=?",String.class,user.uuid()));
        Object principal = org.springframework.security.core.userdetails.User.withUsername(user.username()).password("unused").authorities("USER").build();
        sessions.registerNewSession("recovery-test-"+user.uuid(),principal);
        recovery.reset(secret,"NewPassword2026!","NewPassword2026!");
        assertTrue(sessions.getAllSessions(principal,true).getFirst().isExpired());
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM iam_activity WHERE subject=? AND event='password_reset' AND success", Integer.class, user.uuid().toString()));
        assertEquals(2,messages.size());
        assertTrue(messages.getLast().getSubject().contains("was changed"));
        assertThrows(ApplicationException.class, () -> recovery.reset(secret,PASSWORD,PASSWORD));
    }

    @Test void anonymousPagesRenderAndPostsRequireCsrf() throws Exception {
        int port = Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));
        var http = HttpClient.newHttpClient();
        String base = "http://localhost:"+port;
        var page = http.send(HttpRequest.newBuilder(URI.create(base+"/forgot-password")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,page.statusCode());
        assertTrue(page.body().contains("\"page\":\"forgot-password\""));
        var invalid = http.send(HttpRequest.newBuilder(URI.create(base+"/reset-password")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,invalid.statusCode());
        assertTrue(invalid.body().contains("invalid, expired"));
        for (String path : new String[]{"/forgot-password","/reset-password"}) {
            var post = http.send(HttpRequest.newBuilder(URI.create(base+path)).POST(HttpRequest.BodyPublishers.ofString("email=test@example.com")).header("Content-Type","application/x-www-form-urlencoded").build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(403,post.statusCode());
        }
    }
}
