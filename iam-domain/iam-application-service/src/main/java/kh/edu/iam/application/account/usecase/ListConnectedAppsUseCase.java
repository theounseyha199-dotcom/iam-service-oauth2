package kh.edu.iam.application.account.usecase;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import kh.edu.iam.application.account.result.ConnectedAppResult;
import kh.edu.iam.application.port.out.ConnectedApplications;

@RequiredArgsConstructor
public final class ListConnectedAppsUseCase {
    private final ConnectedApplications applications;

    public List<ConnectedAppResult> execute(UUID userUuid) {
        return applications.list(userUuid);
    }
}
