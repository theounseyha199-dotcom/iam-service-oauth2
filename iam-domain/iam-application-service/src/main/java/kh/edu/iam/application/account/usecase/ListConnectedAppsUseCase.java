package kh.edu.iam.application.account.usecase;

import kh.edu.iam.application.port.out.ConnectedApplications;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ListConnectedAppsUseCase {
    private final ConnectedApplications applications;

    public java.util.List<kh.edu.iam.application.account.result.ConnectedAppResult> execute(String username) {
        return applications.list(username);
    }
}
