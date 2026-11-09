package kh.edu.iam.application.identity.result;

import kh.edu.iam.application.identity.command.FederatedIdentityCommand;

import java.time.Instant;

/**
 * Validated provider identity awaiting the missing profile fields.
 */
public record PendingFederation(FederatedIdentityCommand identity, Instant expiresAt, String flow) {
    public static final String SESSION_ATTRIBUTE = "iam.federation.pending";

    @Override
    public String toString() {
        return "PendingFederation[identity=redacted]";
    }
}
