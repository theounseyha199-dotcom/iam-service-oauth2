package kh.edu.iam.infrastructure.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Atomic Redis operations keep expiry, retries and replacement consistent across instances. */
@Component
public class RedisVerificationCodes {
    private final StringRedisTemplate redis;
    private static final DefaultRedisScript<Long> ISSUE = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[2]) == 1 then return 0 end
            redis.call('HSET', KEYS[1], 'salt', ARGV[1], 'hash', ARGV[2], 'email', ARGV[3], 'attempts', 0)
            redis.call('EXPIRE', KEYS[1], 300)
            redis.call('SET', KEYS[2], ARGV[1], 'EX', 60)
            return 1
            """, Long.class);
    private static final DefaultRedisScript<Long> CONSUME = new DefaultRedisScript<>("""
            if redis.call('HGET', KEYS[1], 'salt') ~= ARGV[1] then return 0 end
            if redis.call('HGET', KEYS[1], 'email') ~= ARGV[3] then return 0 end
            local attempts = redis.call('HINCRBY', KEYS[1], 'attempts', 1)
            if attempts > 5 then redis.call('DEL', KEYS[1]); return 0 end
            if redis.call('HGET', KEYS[1], 'hash') == ARGV[2] then
              redis.call('DEL', KEYS[1]); return 1
            end
            if attempts == 5 then redis.call('DEL', KEYS[1]) end
            return 0
            """, Long.class);
    private static final DefaultRedisScript<Long> CANCEL = new DefaultRedisScript<>("""
            if redis.call('HGET', KEYS[1], 'salt') == ARGV[1] then redis.call('DEL', KEYS[1]) end
            if redis.call('GET', KEYS[2]) == ARGV[1] then redis.call('DEL', KEYS[2]) end
            return 1
            """, Long.class);

    public RedisVerificationCodes(StringRedisTemplate redis) { this.redis = redis; }

    public static String key(UUID user) { return "iam:email-verification:{" + user + "}:otp"; }
    public static String cooldownKey(UUID user) { return "iam:email-verification:{" + user + "}:cooldown"; }

    public String issue(UUID user, String email, String code) {
        String salt = UUID.randomUUID().toString();
        Long issued = redis.execute(ISSUE, List.of(key(user), cooldownKey(user)), salt, digest(salt, email, code), email);
        return Long.valueOf(1).equals(issued) ? salt : null;
    }

    public boolean consume(UUID user, String email, String code) {
        Object salt = redis.opsForHash().get(key(user), "salt");
        if (!(salt instanceof String value)) return false;
        // Even malformed submissions count towards the same bounded attempt budget.
        String candidate = code != null && code.matches("[0-9]{6}") ? code : "invalid";
        return Long.valueOf(1).equals(redis.execute(CONSUME, List.of(key(user)), value,
                digest(value, email, candidate), email));
    }

    public void cancel(UUID user, String issuance) {
        redis.execute(CANCEL, List.of(key(user), cooldownKey(user)), issuance);
    }

    private static String digest(String salt, String email, String code) {
        return EmailPasswordRecovery.hash(salt + ":" + email + ":" + code);
    }
}
