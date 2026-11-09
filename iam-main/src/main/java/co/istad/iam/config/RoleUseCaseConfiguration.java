package co.istad.iam.config;

import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.role.usecase.ActivateRoleUseCase;
import kh.edu.iam.application.role.usecase.AssignPermissionsToRoleUseCase;
import kh.edu.iam.application.role.usecase.CreateRoleUseCase;
import kh.edu.iam.application.role.usecase.DeactivateRoleUseCase;
import kh.edu.iam.application.role.usecase.DeleteRoleUseCase;
import kh.edu.iam.application.role.usecase.GetRoleUseCase;
import kh.edu.iam.application.role.usecase.ListRolesUseCase;
import kh.edu.iam.application.role.usecase.RevokePermissionFromRoleUseCase;
import kh.edu.iam.application.role.usecase.UpdateRoleUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class RoleUseCaseConfiguration {

    @Bean
    public ActivateRoleUseCase activateRoleUseCase(
            TransactionRunner transactions,
            RoleRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock) {
        return new ActivateRoleUseCase(transactions, repository, actorProvider, clock);
    }

    @Bean
    public AssignPermissionsToRoleUseCase assignPermissionsToRoleUseCase(
            RolePermissionRepository repository,
            CurrentActorProvider actorProvider,
            TransactionRunner transactions,
            Clock clock) {
        return new AssignPermissionsToRoleUseCase(repository, actorProvider, transactions, clock);
    }

    @Bean
    public CreateRoleUseCase createRoleUseCase(
            RoleRepository roleRepository,
            AssignPermissionsToRoleUseCase assignPermissions,
            CurrentActorProvider actorProvider,
            TransactionRunner transactions,
            Clock clock) {
        return new CreateRoleUseCase(roleRepository, assignPermissions, actorProvider, transactions, clock);
    }

    @Bean
    public DeactivateRoleUseCase deactivateRoleUseCase(
            TransactionRunner transactions,
            RoleRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock) {
        return new DeactivateRoleUseCase(transactions, repository, actorProvider, clock);
    }

    @Bean
    public DeleteRoleUseCase deleteRoleUseCase(
            TransactionRunner transactions,
            RoleRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock) {
        return new DeleteRoleUseCase(transactions, repository, actorProvider, clock);
    }

    @Bean
    public GetRoleUseCase getRoleUseCase(TransactionRunner transactions, RoleRepository repository) {
        return new GetRoleUseCase(transactions, repository);
    }

    @Bean
    public ListRolesUseCase listRolesUseCase(TransactionRunner transactions, RoleRepository repository) {
        return new ListRolesUseCase(transactions, repository);
    }

    @Bean
    public RevokePermissionFromRoleUseCase revokePermissionFromRoleUseCase(
            RolePermissionRepository repository,
            CurrentActorProvider actorProvider,
            TransactionRunner transactions,
            Clock clock) {
        return new RevokePermissionFromRoleUseCase(repository, actorProvider, transactions, clock);
    }

    @Bean
    public UpdateRoleUseCase updateRoleUseCase(
            TransactionRunner transactions,
            RoleRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock) {
        return new UpdateRoleUseCase(transactions, repository, actorProvider, clock);
    }
}
