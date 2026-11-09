package kh.edu.iam.infrastructure.config;

import kh.edu.iam.application.account.usecase.GetConsentClientUseCase;
import kh.edu.iam.application.account.usecase.ListConnectedAppsUseCase;
import kh.edu.iam.application.account.usecase.RevokeConnectedAppUseCase;
import kh.edu.iam.application.port.out.ConnectedApplications;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectedApplicationsConfig {
    @Bean
    public GetConsentClientUseCase consentClient(ConnectedApplications applications) {
        return new GetConsentClientUseCase(applications);
    }

    @Bean
    public ListConnectedAppsUseCase listConnectedApps(ConnectedApplications applications) {
        return new ListConnectedAppsUseCase(applications);
    }

    @Bean
    public RevokeConnectedAppUseCase revokeConnectedApp(ConnectedApplications applications) {
        return new RevokeConnectedAppUseCase(applications);
    }
}
