package kh.edu.iam.application.account.usecase;

import kh.edu.iam.application.account.result.ConnectedAppResult;
import kh.edu.iam.application.port.out.ConnectedApplications;

import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public final class GetConsentClientUseCase {
    private final ConnectedApplications applications;

    public ConnectedAppResult execute(String clientId, Set<String> scopes) {
        return applications.describe(clientId, scopes);
    }
}
