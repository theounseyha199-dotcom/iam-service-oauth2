package kh.edu.iam.application.account.usecase;

import kh.edu.iam.application.port.out.ConnectedApplications;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RevokeConnectedAppUseCase {
    private final ConnectedApplications applications;

    public void execute(String username, String applicationId) {
        applications.revoke(username, applicationId);
    }
}
