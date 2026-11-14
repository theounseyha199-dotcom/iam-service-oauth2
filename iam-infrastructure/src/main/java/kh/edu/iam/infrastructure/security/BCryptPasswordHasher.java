package kh.edu.iam.infrastructure.security;

import kh.edu.iam.application.common.PasswordPolicy;
import kh.edu.iam.application.port.out.PasswordHasher;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    public BCryptPasswordHasher(org.springframework.jdbc.core.JdbcTemplate jdbc) { this.jdbc=jdbc; }
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public String hash(String rawPassword) {
        PasswordPolicy.validate(rawPassword, rawPassword);
        var policy=jdbc.queryForMap("SELECT password_minimum,require_number,require_uppercase FROM iam_workspace_policy WHERE id=1");
        if(rawPassword.length() < ((Number)policy.get("password_minimum")).intValue()
                || Boolean.TRUE.equals(policy.get("require_number")) && rawPassword.chars().noneMatch(Character::isDigit)
                || Boolean.TRUE.equals(policy.get("require_uppercase")) && rawPassword.chars().noneMatch(Character::isUpperCase)) {
            throw new kh.edu.iam.application.exception.ApplicationException("validation", "Password does not meet the configured policy");
        }
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return rawPassword != null && passwordHash != null && encoder.matches(rawPassword, passwordHash);
    }
}
