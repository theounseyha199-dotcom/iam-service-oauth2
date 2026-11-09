package kh.edu.iam.application.user.usecase;

import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserRepository;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class RecordLoginAttemptUseCase {
    private final UserRepository users;
    private final TransactionRunner transactions;
    private final Clock clock;

    public void execute(String usernameOrEmail, boolean success) {
        transactions.required(
                () -> {
                    var found = users.lockByUsernameOrEmail(Objects.requireNonNull(usernameOrEmail));
                    if (found.isPresent()) {
                        var user = found.get();
                        user.recordLoginAttempt(success, "authentication", clock.instant());
                        users.save(user);
                    }
                    return null;
                });
    }
}
