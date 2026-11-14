package kh.edu.iam.application.port.out;

import java.util.UUID;

public interface EmailVerification {
    void enroll(UUID userUuid);
    void request(String email);
    void verify(String email, String code);
    boolean isPending(UUID userUuid);
}
