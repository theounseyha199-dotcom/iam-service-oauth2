package co.istad.iam.config;

import kh.edu.iam.application.permission.usecase.ActivatePermissionUseCase;
import kh.edu.iam.application.permission.usecase.CreatePermissionUseCase;
import kh.edu.iam.application.permission.usecase.DeactivatePermissionUseCase;
import kh.edu.iam.application.permission.usecase.DeletePermissionUseCase;
import kh.edu.iam.application.permission.usecase.GetPermissionUseCase;
import kh.edu.iam.application.permission.usecase.ListPermissionsUseCase;
import kh.edu.iam.application.permission.usecase.UpdatePermissionUseCase;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.application.port.out.TransactionRunner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class PermissionUseCaseConfiguration {

    @Bean
    public ActivatePermissionUseCase activatePermissionUseCase(
            TransactionRunner transactions,
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock) {
        return new ActivatePermissionUseCase(transactions, repository, actorProvider, clock);
    }

    @Bean
    public CreatePermissionUseCase createPermissionUseCase(
            TransactionRunner transactions,
            PermissionRepository permissionRepository,
            CurrentActorProvider currentActorProvider,
            Clock clock) {
        return new CreatePermissionUseCase(transactions, permissionRepository, currentActorProvider, clock);
    }

    @Bean
    public DeactivatePermissionUseCase deactivatePermissionUseCase(
            TransactionRunner transactions,
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock) {
        return new DeactivatePermissionUseCase(transactions, repository, actorProvider, clock);
    }

    @Bean
    public DeletePermissionUseCase deletePermissionUseCase(
            TransactionRunner transactions,
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock) {
        return new DeletePermissionUseCase(transactions, repository, actorProvider, clock);
    }

    @Bean
    public GetPermissionUseCase getPermissionUseCase(TransactionRunner transactions, PermissionRepository repository) {
        return new GetPermissionUseCase(transactions, repository);
    }

    @Bean
    public ListPermissionsUseCase listPermissionsUseCase(
            TransactionRunner transactions, PermissionRepository repository) {
        return new ListPermissionsUseCase(transactions, repository);
    }

    @Bean
    public UpdatePermissionUseCase updatePermissionUseCase(
            TransactionRunner transactions,
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock) {
        return new UpdatePermissionUseCase(transactions, repository, actorProvider, clock);
    }
}
