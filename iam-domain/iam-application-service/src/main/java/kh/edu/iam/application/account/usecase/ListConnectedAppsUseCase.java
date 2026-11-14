package kh.edu.iam.application.account.usecase;

import kh.edu.iam.application.account.result.ConnectedAppResult;
import kh.edu.iam.application.port.out.ConnectedApplications;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class ListConnectedAppsUseCase {
    private final ConnectedApplications applications;

    public List<ConnectedAppResult> execute(String username) {
        return applications.list(username);
    }
}
