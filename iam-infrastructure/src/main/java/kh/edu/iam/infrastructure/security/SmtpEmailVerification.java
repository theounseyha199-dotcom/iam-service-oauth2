package kh.edu.iam.infrastructure.security;

import kh.edu.iam.application.port.out.EmailVerification;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Locale;
import java.util.UUID;

@Service
public class SmtpEmailVerification implements EmailVerification {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SmtpEmailVerification.class);
    private final UserRepository users;
    private final TransactionRunner transactions;
    private final JdbcTemplate jdbc;
    private final RedisVerificationCodes codes;
    private final EmailService emailService;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public SmtpEmailVerification(UserRepository users, TransactionRunner transactions, JdbcTemplate jdbc,
            RedisVerificationCodes codes, EmailService emailService, Clock clock) {
        this.users = users;
        this.transactions = transactions;
        this.jdbc = jdbc;
        this.codes = codes;
        this.emailService = emailService;
        this.clock = clock;
    }

    @Override public void enroll(UUID userUuid) {
        jdbc.update("INSERT INTO email_verification_required(user_uuid) VALUES (?)", userUuid);
    }

    @Override public boolean isPending(UUID userUuid) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM email_verification_required v JOIN users u ON u.uuid=v.user_uuid WHERE u.uuid=? AND NOT u.email_verified)",
                Boolean.class, userUuid));
    }

    @Override public void request(String email) {
        // Registration has already committed. Redis/SMTP failures leave a recoverable pending account.
        try {
            String address = normalize(email);
            transactions.required(() -> {
                var found = users.lockByUsernameOrEmail(address);
                if (found.isEmpty()) return null;
                var user = found.get();
                if (!address.equals(user.getEmail()) || user.isEmailVerified()
                        || !user.isEnabled() || user.getDeletion().deleted()) return null;
                String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
                String issuance = codes.issue(user.getUuid(), address, code);
                if (issuance == null) return null;
                try {
                    emailService.sendVerificationCode(address, code);
                } catch (RuntimeException ex) {
                    // Conditional cleanup cannot delete a newer code issued by another request.
                    codes.cancel(user.getUuid(), issuance);
                    throw ex;
                }
                return null;
            });
        } catch (RuntimeException ex) {
            LOG.warn("Verification email delivery failed ({})", ex.getClass().getSimpleName());
        }
    }

    @Override public void verify(String email, String code) {
        String address = normalize(email);
        if (address.isEmpty()) throw invalid();
        transactions.required(() -> {
            var user = users.lockByUsernameOrEmail(address).orElseThrow(SmtpEmailVerification::invalid);
            if (!address.equals(user.getEmail()) || user.getDeletion().deleted() || user.isEmailVerified()) throw invalid();
            if (!codes.consume(user.getUuid(), address, code)) throw invalid();
            // Verification never overrides an administrator's enabled/locked decision.
            user.verifyEmail("email-verification", clock.instant());
            users.save(user);
            return null;
        });
    }

    private static String normalize(String email) { return email == null ? "" : email.strip().toLowerCase(Locale.ROOT); }
    private static ApplicationException invalid() {
        return new ApplicationException("invalid_verification_code", "Verification code is invalid or expired");
    }
}
