package kh.edu.iam.application.port.out;

/** Recovery operations are atomic; raw tokens must never be persisted or logged. */
public interface PasswordRecovery {
    void request(String email);
    void reset(String token, String password, String confirmation);
}
