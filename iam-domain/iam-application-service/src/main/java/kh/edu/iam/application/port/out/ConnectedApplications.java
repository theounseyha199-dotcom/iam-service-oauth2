package kh.edu.iam.application.port.out;

import kh.edu.iam.application.account.result.ConnectedAppResult;

import java.util.List;

/**
 * Authorization adapter state; callers supply the authenticated internal user's canonical username.
 */
public interface ConnectedApplications {
    List<ConnectedAppResult> list(String username);

    ConnectedAppResult describe(String clientId, java.util.Set<String> requestedScopes);

    void revoke(String username, String applicationId);
}
