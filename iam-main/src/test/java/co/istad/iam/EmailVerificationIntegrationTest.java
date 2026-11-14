package co.istad.iam;

import static org.junit.jupiter.api.Assertions.*;

import kh.edu.iam.application.port.out.*;
import kh.edu.iam.application.user.command.RegisterUserCommand;
import kh.edu.iam.application.user.result.UserResult;
import kh.edu.iam.application.user.usecase.*;
import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.infrastructure.security.SmtpEmailVerification;
import kh.edu.iam.infrastructure.security.RedisVerificationCodes;
import kh.edu.iam.infrastructure.security.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import java.time.*;
import java.util.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.concurrent.*;

class EmailVerificationIntegrationTest extends IamIntegrationTestSupport {
    private UserResult pending() {
        String name = unique();
        return bean(RegisterUserUseCase.class).execute(new RegisterUserCommand(name,name+"@example.com",PASSWORD,PASSWORD,"Dara","Sok","en"));
    }
    private StringRedisTemplate redis() { return bean(StringRedisTemplate.class); }
    private void clearCode(UserResult user) { redis().delete(List.of(RedisVerificationCodes.key(user.uuid()), RedisVerificationCodes.cooldownKey(user.uuid()))); }
    private SmtpEmailVerification sender(List<SimpleMailMessage> messages) {
        var mail = new JavaMailSenderImpl() {
            @Override public void send(SimpleMailMessage message) { messages.add(message); }
        };
        return sender(mail);
    }
    private SmtpEmailVerification sender(JavaMailSender mail) {
        var factory = new StaticListableBeanFactory(); factory.addBean("mail", mail);
        return new SmtpEmailVerification(bean(UserRepository.class),bean(TransactionRunner.class),jdbc,
                bean(RedisVerificationCodes.class), new EmailService(factory.getBeanProvider(JavaMailSender.class),"no-reply@example.com"), Clock.systemUTC());
    }
    private String code(List<SimpleMailMessage> messages) {
        var match = Pattern.compile("code is: ([0-9]{6})").matcher(messages.getLast().getText());
        assertTrue(match.find()); return match.group(1);
    }
    private void invalid(SmtpEmailVerification verification, String email, String code) {
        assertEquals("invalid_verification_code", assertThrows(ApplicationException.class, () -> verification.verify(email, code)).code());
    }
    private void allowResend(UserResult user) { redis().delete(RedisVerificationCodes.cooldownKey(user.uuid())); }

    @Test void registrationStoresOnlyHashInRedisWithFiveMinuteTtlAndVerificationIsSingleUse() {
        var user = pending(); clearCode(user);
        assertFalse(user.emailVerified());
        assertTrue(bean(EmailVerification.class).isPending(user.uuid()));
        assertFalse(bean(LoadUserForAuthenticationUseCase.class).execute(user.email()).enabled());
        var messages = new ArrayList<SimpleMailMessage>(); var verification = sender(messages);
        verification.request(user.email());
        assertEquals(1,messages.size()); String code = code(messages);
        var values = redis().opsForHash().entries(RedisVerificationCodes.key(user.uuid()));
        assertFalse(values.containsValue(code)); assertTrue(values.get("hash").toString().matches("[a-f0-9]{64}"));
        long ttl = redis().getExpire(RedisVerificationCodes.key(user.uuid())); assertTrue(ttl > 290 && ttl <= 300);
        long cooldown = redis().getExpire(RedisVerificationCodes.cooldownKey(user.uuid())); assertTrue(cooldown > 50 && cooldown <= 60);
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM email_verification_tokens WHERE user_uuid=?", Integer.class, user.uuid()));
        verification.verify(user.email(),code);
        assertTrue(bean(GetUserUseCase.class).execute(user.uuid()).emailVerified());
        assertTrue(bean(LoadUserForAuthenticationUseCase.class).execute(user.email()).enabled());
        assertFalse(redis().hasKey(RedisVerificationCodes.key(user.uuid())));
        invalid(verification,user.email(),code);
    }

    @Test void resendReplacesCodeAndExpiredCodesFailClosed() {
        var user = pending(); clearCode(user);
        var messages = new ArrayList<SimpleMailMessage>(); var verification = sender(messages);
        verification.request("missing@example.com"); assertTrue(messages.isEmpty());
        verification.request(user.email()); String old = code(messages);
        verification.request(user.email()); assertEquals(1,messages.size());
        allowResend(user); verification.request(user.email()); assertEquals(2,messages.size());
        // A six-digit code may repeat by chance; replacement's fresh salt is always independent.
        String current = code(messages);
        if (!old.equals(current)) invalid(verification,user.email(),old);
        redis().expire(RedisVerificationCodes.key(user.uuid()), Duration.ZERO);
        invalid(verification,user.email(),current);
        assertTrue(verification.isPending(user.uuid()));
        allowResend(user); verification.request(user.email()); verification.verify(user.email(),code(messages));
        assertFalse(verification.isPending(user.uuid()));
        verification.request(user.email()); assertEquals(3,messages.size());
    }

    @Test void fiveIncorrectAttemptsInvalidateEvenTheCorrectCodeAndMalformedAttemptsCount() {
        var user = pending(); clearCode(user);
        var messages = new ArrayList<SimpleMailMessage>(); var verification = sender(messages);
        verification.request(user.email()); String correct = code(messages);
        String wrong = correct.equals("000000") ? "000001" : "000000";
        for (int i=0; i<4; i++) invalid(verification,user.email(),wrong);
        assertTrue(redis().hasKey(RedisVerificationCodes.key(user.uuid())));
        invalid(verification,user.email(),"not-a-code");
        invalid(verification,user.email(),correct);
        assertTrue(verification.isPending(user.uuid()));
        assertFalse(redis().hasKey(RedisVerificationCodes.key(user.uuid())));
    }

    @Test void codesAreBoundToTheCurrentEmailAndCannotEnableDisabledAccounts() {
        var user = pending(); clearCode(user);
        var messages = new ArrayList<SimpleMailMessage>(); var verification = sender(messages);
        verification.request(user.email()); String old = code(messages);
        String newEmail = unique()+"@example.com";
        bean(TransactionRunner.class).required(() -> {
            var repository = bean(UserRepository.class); var current = repository.lockByUuid(user.uuid()).orElseThrow();
            current.changeEmail(newEmail,"test",Instant.now()); repository.save(current); return null;
        });
        invalid(verification,user.email(),old); invalid(verification,newEmail,old);
        allowResend(user); verification.request(newEmail); String valid = code(messages);
        bean(DisableUserUseCase.class).execute(user.uuid());
        verification.verify(newEmail,valid);
        assertTrue(bean(GetUserUseCase.class).execute(user.uuid()).emailVerified());
        assertFalse(bean(LoadUserForAuthenticationUseCase.class).execute(newEmail).enabled());
    }

    @Test void smtpFailureLeavesAccountPendingAndAllowsRetryWithoutAStoredCode() {
        var user = pending(); clearCode(user);
        var failing = sender(new JavaMailSenderImpl() {
            @Override public void send(SimpleMailMessage message) { throw new org.springframework.mail.MailSendException("test failure"); }
        });
        assertDoesNotThrow(() -> failing.request(user.email()));
        assertFalse(redis().hasKey(RedisVerificationCodes.key(user.uuid())));
        assertFalse(redis().hasKey(RedisVerificationCodes.cooldownKey(user.uuid())));
        assertTrue(failing.isPending(user.uuid()));
        var messages = new ArrayList<SimpleMailMessage>(); var verification = sender(messages);
        verification.request(user.email()); verification.verify(user.email(),code(messages));
        assertFalse(verification.isPending(user.uuid()));
    }

    @Test void unavailableRedisCannotVerifyOrEnableAnAccount() {
        var user = pending(); clearCode(user);
        var unavailable = new RedisVerificationCodes(redis()) {
            @Override public String issue(UUID id, String email, String code) {
                throw new org.springframework.data.redis.RedisConnectionFailureException("test unavailable");
            }
            @Override public boolean consume(UUID id, String email, String code) {
                throw new org.springframework.data.redis.RedisConnectionFailureException("test unavailable");
            }
        };
        var factory = new StaticListableBeanFactory();
        factory.addBean("mail", new JavaMailSenderImpl());
        var verification = new SmtpEmailVerification(bean(UserRepository.class), bean(TransactionRunner.class), jdbc,
                unavailable, new EmailService(factory.getBeanProvider(JavaMailSender.class), "no-reply@example.com"), Clock.systemUTC());
        assertDoesNotThrow(() -> verification.request(user.email()));
        assertThrows(org.springframework.data.redis.RedisConnectionFailureException.class,
                () -> verification.verify(user.email(), "123456"));
        assertTrue(verification.isPending(user.uuid()));
        assertFalse(bean(GetUserUseCase.class).execute(user.uuid()).emailVerified());
        assertFalse(bean(LoadUserForAuthenticationUseCase.class).execute(user.email()).enabled());
    }

    @Test void concurrentConsumptionAndIssuanceAreAtomicInRedis() throws Exception {
        UUID user = UUID.randomUUID(); var store = bean(RedisVerificationCodes.class);
        String email = "atomic@example.com";
        try (var executor = Executors.newFixedThreadPool(8)) {
            var issued = executor.invokeAll(java.util.stream.IntStream.range(0,8).mapToObj(i -> (Callable<Boolean>) () -> store.issue(user,email,"123456") != null).toList());
            assertEquals(1, issued.stream().filter(f -> { try { return f.get(); } catch (Exception e) { throw new RuntimeException(e); } }).count());
            var consumed = executor.invokeAll(java.util.stream.IntStream.range(0,8).mapToObj(i -> (Callable<Boolean>) () -> store.consume(user,email,"123456")).toList());
            assertEquals(1, consumed.stream().filter(f -> { try { return f.get(); } catch (Exception e) { throw new RuntimeException(e); } }).count());
        }
    }

    @Test void browserRequiresCsrfDoesNotEchoCodeAndEnablesLoginOnlyAfterVerification() throws Exception {
        var user = pending(); clearCode(user);
        var messages = new ArrayList<SimpleMailMessage>(); sender(messages).request(user.email()); String secret = code(messages);
        String base = "http://localhost:"+context.getEnvironment().getProperty("local.server.port");
        var http = HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
        assertTrue(loginLocation(http,base,user).contains("error"));
        var page = http.send(HttpRequest.newBuilder(URI.create(base+"/verify-email")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,page.statusCode()); assertTrue(bean(EmailVerification.class).isPending(user.uuid()));
        var csrf = Pattern.compile("\"csrfToken\":\"([^\"]+)\"").matcher(page.body()); assertTrue(csrf.find());
        String body = "email="+URLEncoder.encode(user.email(),StandardCharsets.UTF_8)+"&code="+secret;
        var denied = http.send(HttpRequest.newBuilder(URI.create(base+"/verify-email")).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString()); assertEquals(403,denied.statusCode());
        var verified = http.send(HttpRequest.newBuilder(URI.create(base+"/verify-email")).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body+"&_csrf="+URLEncoder.encode(csrf.group(1),StandardCharsets.UTF_8))).build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,verified.statusCode());
        assertFalse(verified.body().contains("\"code\":\""+secret));
        assertFalse(bean(EmailVerification.class).isPending(user.uuid()));
        assertFalse(loginLocation(http,base,user).contains("error"));
    }

    private String loginLocation(HttpClient http,String base,UserResult user) throws Exception {
        var page = http.send(HttpRequest.newBuilder(URI.create(base+"/login")).GET().build(),HttpResponse.BodyHandlers.ofString());
        var csrf = Pattern.compile("\"csrfToken\":\"([^\"]+)\"").matcher(page.body()); assertTrue(csrf.find());
        String body = "username="+URLEncoder.encode(user.email(),StandardCharsets.UTF_8)+"&password="+URLEncoder.encode(PASSWORD,StandardCharsets.UTF_8)+"&_csrf="+URLEncoder.encode(csrf.group(1),StandardCharsets.UTF_8);
        var result = http.send(HttpRequest.newBuilder(URI.create(base+"/login")).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(302,result.statusCode()); return result.headers().firstValue("Location").orElseThrow();
    }

    @Test void pendingAccountCannotIssueTokensAndLegacyAccountKeepsAccess() {
        var user = pending();
        var principal = new kh.edu.iam.infrastructure.security.IamUserPrincipal(user.uuid(),user.username(),Set.of("ROLE_USER"),true,true,true,true);
        var authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(principal,null,List.of());
        var encoding = org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext.with(
                org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256),
                org.springframework.security.oauth2.jwt.JwtClaimsSet.builder())
                .registeredClient(bean(org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository.class).findByClientId("iam-web-client"))
                .principal(authentication).tokenType(org.springframework.security.oauth2.server.authorization.OAuth2TokenType.ACCESS_TOKEN).authorizedScopes(Set.of()).build();
        var customizer = new kh.edu.iam.infrastructure.config.TokenConfig().jwtCustomizer(bean(GetUserUseCase.class),bean(GetUserAuthoritiesUseCase.class),bean(kh.edu.iam.infrastructure.properties.TokenAudienceProperties.class),bean(EmailVerification.class));
        assertThrows(org.springframework.security.oauth2.core.OAuth2AuthenticationException.class, () -> customizer.customize(encoding));
        jdbc.update("DELETE FROM email_verification_required WHERE user_uuid=?",user.uuid());
        assertTrue(bean(LoadUserForAuthenticationUseCase.class).execute(user.email()).enabled());
        assertDoesNotThrow(() -> customizer.customize(encoding));
    }

    @Test void legacyUnverifiedAccountCanRequestCodeWithoutChangingItsSignInPolicy() {
        var user = pending(); clearCode(user); jdbc.update("DELETE FROM email_verification_required WHERE user_uuid=?",user.uuid());
        var messages = new ArrayList<SimpleMailMessage>(); var verification = sender(messages);
        assertFalse(verification.isPending(user.uuid()));
        verification.request(user.email()); verification.verify(user.email(),code(messages));
        assertTrue(bean(GetUserUseCase.class).execute(user.uuid()).emailVerified());
        assertTrue(bean(LoadUserForAuthenticationUseCase.class).execute(user.email()).enabled());
    }
}
