package co.istad.iam.config;

import kh.edu.iam.application.port.out.EmailVerification;

import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PasswordHasher;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.application.port.out.RolePermissionRepository;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserAssignmentRepository;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.service.UserApplicationSupport;
import kh.edu.iam.application.user.usecase.AssignRolesToUserUseCase;
import kh.edu.iam.application.user.usecase.ChangePasswordUseCase;
import kh.edu.iam.application.user.usecase.CreateUserUseCase;
import kh.edu.iam.application.user.usecase.DeleteUserUseCase;
import kh.edu.iam.application.user.usecase.DisableUserUseCase;
import kh.edu.iam.application.user.usecase.EnableUserUseCase;
import kh.edu.iam.application.user.usecase.GetUserAuthoritiesUseCase;
import kh.edu.iam.application.user.usecase.GetUserUseCase;
import kh.edu.iam.application.user.usecase.GrantPermissionsToUserUseCase;
import kh.edu.iam.application.user.usecase.ListUsersUseCase;
import kh.edu.iam.application.user.usecase.LoadUserForAuthenticationUseCase;
import kh.edu.iam.application.user.usecase.LockUserUseCase;
import kh.edu.iam.application.user.usecase.RecordLoginAttemptUseCase;
import kh.edu.iam.application.user.usecase.RegisterUserUseCase;
import kh.edu.iam.application.user.usecase.ResetUserPasswordUseCase;
import kh.edu.iam.application.user.usecase.RevokePermissionFromUserUseCase;
import kh.edu.iam.application.user.usecase.RevokeRoleFromUserUseCase;
import kh.edu.iam.application.user.usecase.UnlockUserUseCase;
import kh.edu.iam.application.user.usecase.UpdateUserProfileUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class UserUseCaseConfiguration {

    @Bean
    public AssignRolesToUserUseCase assignRolesToUserUseCase(
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock,
            UserAssignmentRepository assignments,
            RolePermissionRepository targets) {
        return new AssignRolesToUserUseCase(support, transactions, actors, clock, assignments, targets);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock,
            PasswordHasher passwords) {
        return new ChangePasswordUseCase(users, support, transactions, actors, clock, passwords);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock,
            PasswordHasher passwords,
            AssignRolesToUserUseCase assignRoles) {
        return new CreateUserUseCase(users, support, transactions, actors, clock, passwords, assignRoles);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock) {
        return new DeleteUserUseCase(users, support, transactions, actors, clock);
    }

    @Bean
    public DisableUserUseCase disableUserUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock) {
        return new DisableUserUseCase(users, support, transactions, actors, clock);
    }

    @Bean
    public EnableUserUseCase enableUserUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock) {
        return new EnableUserUseCase(users, support, transactions, actors, clock);
    }

    @Bean
    public GetUserAuthoritiesUseCase getUserAuthoritiesUseCase(
            UserApplicationSupport support, TransactionRunner transactions) {
        return new GetUserAuthoritiesUseCase(support, transactions);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserApplicationSupport support, TransactionRunner transactions) {
        return new GetUserUseCase(support, transactions);
    }

    @Bean
    public GrantPermissionsToUserUseCase grantPermissionsToUserUseCase(
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock,
            UserAssignmentRepository assignments,
            RolePermissionRepository targets) {
        return new GrantPermissionsToUserUseCase(support, transactions, actors, clock, assignments, targets);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserRepository users, TransactionRunner transactions) {
        return new ListUsersUseCase(users, transactions);
    }

    @Bean
    public LoadUserForAuthenticationUseCase loadUserForAuthenticationUseCase(
            UserRepository users, UserApplicationSupport support, TransactionRunner transactions,
            EmailVerification verification) {
        return new LoadUserForAuthenticationUseCase(users, support, transactions, verification);
    }

    @Bean
    public LockUserUseCase lockUserUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock) {
        return new LockUserUseCase(users, support, transactions, actors, clock);
    }

    @Bean
    public RecordLoginAttemptUseCase recordLoginAttemptUseCase(
            UserRepository users, TransactionRunner transactions, Clock clock) {
        return new RecordLoginAttemptUseCase(users, transactions, clock);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            PasswordHasher passwords,
            RoleRepository roles,
            UserAssignmentRepository assignments,
            Clock clock, EmailVerification verification) {
        return new RegisterUserUseCase(users, support, transactions, passwords, roles, assignments, clock, verification);
    }

    @Bean
    public ResetUserPasswordUseCase resetUserPasswordUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock,
            PasswordHasher passwords) {
        return new ResetUserPasswordUseCase(users, support, transactions, actors, clock, passwords);
    }

    @Bean
    public RevokePermissionFromUserUseCase revokePermissionFromUserUseCase(
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock,
            UserAssignmentRepository assignments,
            PermissionRepository targetRepository) {
        return new RevokePermissionFromUserUseCase(support, transactions, actors, clock, assignments, targetRepository);
    }

    @Bean
    public RevokeRoleFromUserUseCase revokeRoleFromUserUseCase(
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock,
            UserAssignmentRepository assignments,
            RoleRepository targetRepository) {
        return new RevokeRoleFromUserUseCase(support, transactions, actors, clock, assignments, targetRepository);
    }

    @Bean
    public UnlockUserUseCase unlockUserUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock) {
        return new UnlockUserUseCase(users, support, transactions, actors, clock);
    }

    @Bean
    public UpdateUserProfileUseCase updateUserProfileUseCase(
            UserRepository users,
            UserApplicationSupport support,
            TransactionRunner transactions,
            CurrentActorProvider actors,
            Clock clock) {
        return new UpdateUserProfileUseCase(users, support, transactions, actors, clock);
    }
}
