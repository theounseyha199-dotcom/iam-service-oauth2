package co.istad.iam.config;

import kh.edu.iam.application.identity.service.IdentityApplicationSupport;
import kh.edu.iam.application.identity.usecase.FindOrProvisionFederatedUserUseCase;
import kh.edu.iam.application.identity.usecase.LinkIdentityUseCase;
import kh.edu.iam.application.identity.usecase.ListUserIdentitiesUseCase;
import kh.edu.iam.application.identity.usecase.UnlinkIdentityUseCase;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.IdentityRepository;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.service.UserApplicationSupport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class IdentityUseCaseConfiguration {

    @Bean
    public FindOrProvisionFederatedUserUseCase findOrProvisionFederatedUserUseCase(
            IdentityRepository identities,
            UserApplicationSupport users,
            TransactionRunner transactions,
            IdentityApplicationSupport support,
            UserRepository userRepository,
            RoleRepository roles,
            UserAssignmentRepository assignments,
            Clock clock) {
        return new FindOrProvisionFederatedUserUseCase(
                identities, users, transactions, support, userRepository, roles, assignments, clock);
    }

    @Bean
    public LinkIdentityUseCase linkIdentityUseCase(
            IdentityRepository identities,
            UserApplicationSupport users,
            TransactionRunner transactions,
            IdentityApplicationSupport support,
            CurrentActorProvider actors) {
        return new LinkIdentityUseCase(identities, users, transactions, support, actors);
    }

    @Bean
    public ListUserIdentitiesUseCase listUserIdentitiesUseCase(
            IdentityRepository identities, UserApplicationSupport users, TransactionRunner transactions) {
        return new ListUserIdentitiesUseCase(identities, users, transactions);
    }

    @Bean
    public UnlinkIdentityUseCase unlinkIdentityUseCase(
            IdentityRepository identities,
            UserApplicationSupport users,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock) {
        return new UnlinkIdentityUseCase(identities, users, transactions, actors, clock);
    }
}
