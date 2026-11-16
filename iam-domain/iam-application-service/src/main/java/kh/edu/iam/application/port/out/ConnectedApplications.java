package kh.edu.iam.application.port.out;

import java.util.List;
import java.util.UUID;

import kh.edu.iam.application.account.result.ConnectedAppResult;

/**
 * Authorization adapter state; callers supply the authenticated internal user's stable UUID.
 */
public interface ConnectedApplications {
    List<ConnectedAppResult> list(UUID userUuid);

    ConnectedAppResult describe(String clientId, java.util.Set<String> requestedScopes);

    void revoke(UUID userUuid, String applicationId);
}
