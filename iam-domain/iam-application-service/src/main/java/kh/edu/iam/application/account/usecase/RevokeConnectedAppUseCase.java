package kh.edu.iam.application.account.usecase;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import kh.edu.iam.application.port.out.ConnectedApplications;

@RequiredArgsConstructor
public final class RevokeConnectedAppUseCase {
    private final ConnectedApplications applications;

    public void execute(UUID userUuid, String applicationId) {
        applications.revoke(userUuid, applicationId);
    }
}
