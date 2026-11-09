package co.istad.iam;

import static org.junit.jupiter.api.Assertions.*;

import kh.edu.iam.Address;
import kh.edu.iam.IdentityProvider;
import kh.edu.iam.PermissionEffect;
import kh.edu.iam.UserProfile;
import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.identity.usecase.FindOrProvisionFederatedUserUseCase;
import kh.edu.iam.application.identity.usecase.LinkIdentityUseCase;
import kh.edu.iam.application.identity.usecase.ListUserIdentitiesUseCase;
import kh.edu.iam.application.identity.usecase.UnlinkIdentityUseCase;
import kh.edu.iam.application.permission.command.UpdatePermissionCommand;
import kh.edu.iam.application.permission.query.PermissionFilterQuery;
import kh.edu.iam.application.permission.usecase.ActivatePermissionUseCase;
import kh.edu.iam.application.permission.usecase.DeactivatePermissionUseCase;
import kh.edu.iam.application.permission.usecase.DeletePermissionUseCase;
import kh.edu.iam.application.permission.usecase.GetPermissionUseCase;
import kh.edu.iam.application.permission.usecase.ListPermissionsUseCase;
import kh.edu.iam.application.permission.usecase.UpdatePermissionUseCase;
import kh.edu.iam.application.port.out.PasswordHasher;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.role.command.AssignPermissionsToRoleCommand;
import kh.edu.iam.application.role.command.CreateRoleCommand;
import kh.edu.iam.application.role.command.UpdateRoleCommand;
import kh.edu.iam.application.role.query.RoleFilterQuery;
import kh.edu.iam.application.role.usecase.ActivateRoleUseCase;
import kh.edu.iam.application.role.usecase.AssignPermissionsToRoleUseCase;
import kh.edu.iam.application.role.usecase.CreateRoleUseCase;
import kh.edu.iam.application.role.usecase.DeactivateRoleUseCase;
import kh.edu.iam.application.role.usecase.DeleteRoleUseCase;
import kh.edu.iam.application.role.usecase.GetRoleUseCase;
import kh.edu.iam.application.role.usecase.ListRolesUseCase;
import kh.edu.iam.application.role.usecase.RevokePermissionFromRoleUseCase;
import kh.edu.iam.application.role.usecase.UpdateRoleUseCase;
import kh.edu.iam.application.user.command.AssignRolesToUserCommand;
import kh.edu.iam.application.user.command.ChangePasswordCommand;
import kh.edu.iam.application.user.command.CreateUserCommand;
import kh.edu.iam.application.user.command.GrantPermissionsToUserCommand;
import kh.edu.iam.application.user.command.RegisterUserCommand;
import kh.edu.iam.application.user.command.ResetUserPasswordCommand;
import kh.edu.iam.application.user.command.UpdateUserProfileCommand;
import kh.edu.iam.application.user.query.UserFilterQuery;
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

import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Never runs against the normal application database.
 */
class PartTwoIntegrationTest extends IamIntegrationTestSupport {
    @Test
    void migrationsSeedsAndAllSectionCBeansExist() {
        assertEquals(
                7,
                jdbc.queryForObject(
                        "select count(*) from information_schema.tables where table_schema='public' and table_name in ('users','roles','permissions','identities','user_roles','role_permissions','user_permissions')",
                        Integer.class));
        assertEquals(14, jdbc.queryForObject("select count(*) from permissions where is_system", Integer.class));
        assertEquals(2, jdbc.queryForObject("select count(*) from roles where is_system", Integer.class));
        assertEquals(
                14,
                jdbc.queryForObject(
                        "select count(*) from role_permissions a join roles r on r.id=a.role_id where r.name='ADMIN'",
                        Integer.class));
        assertEquals(8, jdbc.queryForObject("select count(*) from databasechangelog", Integer.class));
        assertEquals(
                39,
                context.getBeansOfType(Object.class).values().stream()
                        .filter(
                                b ->
                                        b.getClass().getSimpleName().endsWith("UseCase")
                                                && !b.getClass().getPackageName().contains(".account."))
                        .count());
    }

    @Test
    void anonymousRegistrationHashesPasswordAndAssignsOnlyUser() {
        SecurityContextHolder.clearContext();
        var u = register();
        assertEquals("USER", u.roles().getFirst().name());
        assertEquals(1, u.roles().size());
        var auth = bean(LoadUserForAuthenticationUseCase.class).execute(u.email().toUpperCase(Locale.ROOT));
        assertTrue(bean(PasswordHasher.class).matches(PASSWORD, auth.passwordHash()));
        assertNotEquals(PASSWORD, auth.passwordHash());
        assertFalse(auth.toString().contains(auth.passwordHash()));
        assertEquals(Set.of("ROLE_USER"), auth.authorities());
    }

    @Test
    void duplicateAndInvalidRegistrationDoNotCreateRows() {
        var u = register();
        assertThrows(
                DuplicateResourceException.class,
                () ->
                        bean(RegisterUserUseCase.class)
                                .execute(
                                        new RegisterUserCommand(
                                                unique(),
                                                u.email().toUpperCase(Locale.ROOT),
                                                PASSWORD,
                                                PASSWORD,
                                                "A",
                                                "B",
                                                "en")));
        var name = unique();
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        bean(RegisterUserUseCase.class)
                                .execute(
                                        new RegisterUserCommand(
                                                name, name + "@example.com", PASSWORD, "different", "A", "B", "en")));
        assertFalse(bean(UserRepository.class).existsByUsername(name));
    }

    @Test
    void userProfilePaginationStatusAndSoftDelete() {
        var u = register();
        var updated =
                bean(UpdateUserProfileUseCase.class)
                        .execute(
                                new UpdateUserProfileCommand(
                                        u.uuid(),
                                        null,
                                        null,
                                        null,
                                        "Dara",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        "Asia/Phnom_Penh",
                                        "en",
                                        "+85512345678",
                                        new Address("Street", "City", "Region", "12000", "KH")));
        assertEquals("Dara", updated.profile().nickname());
        assertEquals("Sok", updated.profile().familyName());
        assertEquals("KH", bean(GetUserUseCase.class).execute(u.uuid()).profile().address().country());
        assertEquals(
                1,
                bean(ListUsersUseCase.class)
                        .execute(new UserFilterQuery(u.username(), true, 0, 20, "username", false))
                        .totalElements());
        bean(DisableUserUseCase.class).execute(u.uuid());
        assertFalse(bean(GetUserUseCase.class).execute(u.uuid()).enabled());
        bean(EnableUserUseCase.class).execute(u.uuid());
        bean(LockUserUseCase.class).execute(u.uuid());
        assertFalse(bean(GetUserUseCase.class).execute(u.uuid()).accountNonLocked());
        bean(UnlockUserUseCase.class).execute(u.uuid());
        assertTrue(bean(GetUserUseCase.class).execute(u.uuid()).accountNonLocked());
        bean(DeleteUserUseCase.class).execute(u.uuid());
        assertThrows(ResourceNotFoundException.class, () -> bean(GetUserUseCase.class).execute(u.uuid()));
        assertTrue(jdbc.queryForObject("select is_deleted from users where uuid=?", Boolean.class, u.uuid()));
    }

    @Test
    void passwordChangesResetsAndLoginAttempts() {
        var u = register();
        bean(RecordLoginAttemptUseCase.class).execute(u.username(), false);
        assertEquals(
                1,
                jdbc.queryForObject("select failed_login_attempts from users where uuid=?", Integer.class, u.uuid()));
        bean(RecordLoginAttemptUseCase.class).execute(u.username(), true);
        assertEquals(
                0,
                jdbc.queryForObject("select failed_login_attempts from users where uuid=?", Integer.class, u.uuid()));
        bean(RecordLoginAttemptUseCase.class).execute("missing-user", false);
        assertThrows(
                ApplicationException.class,
                () ->
                        bean(ChangePasswordUseCase.class)
                                .execute(
                                        new ChangePasswordCommand(
                                                u.uuid(), "wrong", "Another!Pass2026", "Another!Pass2026")));
        bean(ChangePasswordUseCase.class)
                .execute(new ChangePasswordCommand(u.uuid(), PASSWORD, "Another!Pass2026", "Another!Pass2026"));
        bean(ResetUserPasswordUseCase.class).execute(new ResetUserPasswordCommand(u.uuid(), PASSWORD, PASSWORD));
        assertTrue(
                bean(PasswordHasher.class)
                        .matches(
                                PASSWORD,
                                bean(LoadUserForAuthenticationUseCase.class).execute(u.username()).passwordHash()));
    }

    @Test
    void createUserAndRoleBatchesRollBack() {
        var p = permission();
        var missing = UUID.randomUUID();
        var name = unique();
        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        bean(CreateRoleUseCase.class)
                                .execute(new CreateRoleCommand(name, "Rollback", List.of(p.uuid(), missing))));
        assertFalse(bean(RoleRepository.class).existsByName(name));
        var r = role(List.of(p.uuid()));
        var username = unique();
        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        bean(CreateUserUseCase.class)
                                .execute(
                                        new CreateUserCommand(
                                                username,
                                                username + "@example.com",
                                                PASSWORD,
                                                UserProfile.basic("A", "B", "en"),
                                                List.of(r.uuid(), missing))));
        assertFalse(bean(UserRepository.class).existsByUsername(username));
        var created =
                bean(CreateUserUseCase.class)
                        .execute(
                                new CreateUserCommand(
                                        username,
                                        username + "@example.com",
                                        PASSWORD,
                                        UserProfile.basic("A", "B", "en"),
                                        List.of(r.uuid())));
        assertEquals(r.uuid(), created.roles().getFirst().roleUuid());
    }

    @Test
    void directDenyOverridesRolesAndGrantUntilRevoked() {
        var u = register();
        var p = permission();
        var r = role(List.of(p.uuid()));
        bean(AssignRolesToUserUseCase.class)
                .execute(new AssignRolesToUserCommand(u.uuid(), List.of(r.uuid()), null, "Role"));
        bean(GrantPermissionsToUserUseCase.class)
                .execute(
                        new GrantPermissionsToUserCommand(
                                u.uuid(), List.of(p.uuid()), PermissionEffect.GRANT, null, "Grant"));
        bean(GrantPermissionsToUserUseCase.class)
                .execute(
                        new GrantPermissionsToUserCommand(
                                u.uuid(), List.of(p.uuid()), PermissionEffect.DENY, null, "Deny"));
        assertFalse(bean(GetUserAuthoritiesUseCase.class).execute(u.uuid()).effectivePermissions().contains(p.name()));
        bean(RevokePermissionFromUserUseCase.class).execute(u.uuid(), p.uuid(), "Remove direct overrides");
        assertTrue(bean(GetUserAuthoritiesUseCase.class).execute(u.uuid()).effectivePermissions().contains(p.name()));
        bean(RevokeRoleFromUserUseCase.class).execute(u.uuid(), r.uuid(), "Remove role");
        assertFalse(bean(GetUserAuthoritiesUseCase.class).execute(u.uuid()).effectivePermissions().contains(p.name()));
        assertEquals(
                2,
                jdbc.queryForObject(
                        "select count(*) from user_permissions a join users u on u.id=a.user_id where u.uuid=? and a.revoked_at is not null",
                        Integer.class,
                        u.uuid()));
    }

    @Test
    void assignmentValidationAndExpiry() {
        var u = register();
        var p = permission();
        var r = role(List.of());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        bean(AssignRolesToUserUseCase.class)
                                .execute(
                                        new AssignRolesToUserCommand(
                                                u.uuid(),
                                                List.of(r.uuid()),
                                                Instant.now().minusSeconds(1),
                                                "Expired")));
        bean(DeactivateRoleUseCase.class).execute(r.uuid());
        assertThrows(
                IllegalStateException.class,
                () ->
                        bean(AssignRolesToUserUseCase.class)
                                .execute(new AssignRolesToUserCommand(u.uuid(), List.of(r.uuid()), null, "Inactive")));
        bean(DeactivatePermissionUseCase.class).execute(p.uuid());
        assertThrows(
                IllegalStateException.class,
                () ->
                        bean(GrantPermissionsToUserUseCase.class)
                                .execute(
                                        new GrantPermissionsToUserCommand(
                                                u.uuid(),
                                                List.of(p.uuid()),
                                                PermissionEffect.GRANT,
                                                null,
                                                "Inactive")));
    }

    @Test
    void rolePermissionRevocationKeepsHistoryAndAllowsReassignment() {
        var p = permission();
        var r = role(List.of(p.uuid()));
        bean(RevokePermissionFromRoleUseCase.class).execute(r.uuid(), p.uuid(), "Access removed");
        bean(AssignPermissionsToRoleUseCase.class)
                .execute(new AssignPermissionsToRoleCommand(r.uuid(), List.of(p.uuid()), null, "Access restored"));
        assertEquals(
                2,
                jdbc.queryForObject(
                        "select count(*) from role_permissions a join roles r on r.id=a.role_id where r.uuid=?",
                        Integer.class,
                        r.uuid()));
        assertEquals(
                "Access removed",
                jdbc.queryForObject(
                        "select a.revocation_reason from role_permissions a join roles r on r.id=a.role_id where r.uuid=? and a.revoked_at is not null",
                        String.class,
                        r.uuid()));
    }

    @Test
    void permissionAndRoleCrudSystemProtectionAndOptimisticLocking() {
        var p = permission();
        var r = role(List.of());
        var stale = bean(PermissionRepository.class).findByUuid(p.uuid()).orElseThrow();
        bean(UpdatePermissionUseCase.class).execute(new UpdatePermissionCommand(p.uuid(), "Updated"));
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> bean(PermissionRepository.class).save(stale));
        assertEquals(
                1,
                bean(ListPermissionsUseCase.class)
                        .execute(new PermissionFilterQuery(p.name(), true, 0, 10, "name", false))
                        .totalElements());
        bean(DeactivatePermissionUseCase.class).execute(p.uuid());
        bean(ActivatePermissionUseCase.class).execute(p.uuid());
        assertTrue(bean(GetPermissionUseCase.class).execute(p.uuid()).active());
        bean(UpdateRoleUseCase.class).execute(new UpdateRoleCommand(r.uuid(), r.name(), "Updated"));
        assertEquals(
                1,
                bean(ListRolesUseCase.class)
                        .execute(new RoleFilterQuery(r.name(), true, 0, 10, "name", false))
                        .totalElements());
        bean(DeactivateRoleUseCase.class).execute(r.uuid());
        bean(ActivateRoleUseCase.class).execute(r.uuid());
        assertTrue(bean(GetRoleUseCase.class).execute(r.uuid()).active());
        var admin = bean(RoleRepository.class).findByName("ADMIN").orElseThrow();
        assertThrows(IllegalStateException.class, () -> bean(DeleteRoleUseCase.class).execute(admin.getUuid()));
        bean(DeletePermissionUseCase.class).execute(p.uuid());
        bean(DeleteRoleUseCase.class).execute(r.uuid());
        assertThrows(ResourceNotFoundException.class, () -> bean(GetPermissionUseCase.class).execute(p.uuid()));
        assertThrows(ResourceNotFoundException.class, () -> bean(GetRoleUseCase.class).execute(r.uuid()));
    }

    @Test
    void federationUsesProviderSubjectAndNeverAutoLinksByEmail() {
        var email = unique() + "@example.com";
        var command = identity(IdentityProvider.GOOGLE, unique(), email);
        var first = bean(FindOrProvisionFederatedUserUseCase.class).execute(command);
        var second = bean(FindOrProvisionFederatedUserUseCase.class).execute(command);
        assertEquals(first.uuid(), second.uuid());
        assertNull(first.passwordHash());
        assertTrue(first.authorities().contains("ROLE_USER"));
        assertThrows(
                ApplicationException.class,
                () ->
                        bean(FindOrProvisionFederatedUserUseCase.class)
                                .execute(identity(IdentityProvider.GITHUB, unique(), email)));
        assertEquals(
                "provider-user",
                jdbc.queryForObject(
                        "select attributes->>'id' from identities where provider_subject=?",
                        String.class,
                        command.providerSubject()));
        var linked = bean(ListUserIdentitiesUseCase.class).execute(first.uuid());
        assertThrows(
                IllegalStateException.class,
                () -> bean(UnlinkIdentityUseCase.class).execute(first.uuid(), linked.getFirst().uuid()));
    }

    @Test
    void identityOwnershipProviderUniquenessUnlinkAndRelink() {
        var u = register();
        var other = register();
        var command = identity(IdentityProvider.GITHUB, unique(), u.email());
        var linked = bean(LinkIdentityUseCase.class).execute(u.uuid(), command);
        assertThrows(
                DuplicateResourceException.class, () -> bean(LinkIdentityUseCase.class).execute(other.uuid(), command));
        assertThrows(
                DuplicateResourceException.class,
                () ->
                        bean(LinkIdentityUseCase.class)
                                .execute(u.uuid(), identity(IdentityProvider.GITHUB, unique(), u.email())));
        assertThrows(
                ResourceNotFoundException.class,
                () -> bean(UnlinkIdentityUseCase.class).execute(other.uuid(), linked.uuid()));
        bean(UnlinkIdentityUseCase.class).execute(u.uuid(), linked.uuid());
        assertTrue(bean(ListUserIdentitiesUseCase.class).execute(u.uuid()).isEmpty());
        var relinked = bean(LinkIdentityUseCase.class).execute(u.uuid(), command);
        assertEquals(linked.uuid(), relinked.uuid());
    }

    @Test
    void providerWithoutEmailRequiresAValidatedProfileCompletionFlow() {
        var error =
                assertThrows(
                        ApplicationException.class,
                        () ->
                                bean(FindOrProvisionFederatedUserUseCase.class)
                                        .execute(identity(IdentityProvider.TELEGRAM, unique(), null)));
        assertEquals("profile_completion_required", error.code());
    }

    @Test
    void concurrentRoleAssignmentCreatesOnlyOneActiveRow() throws Exception {
        var u = register();
        var r = role(List.of());
        try (var executor = Executors.newFixedThreadPool(2)) {
            var start = new CountDownLatch(1);
            Callable<Boolean> task =
                    () -> {
                        SecurityContextHolder.getContext()
                                .setAuthentication(
                                        UsernamePasswordAuthenticationToken.authenticated(
                                                "test-actor", null, List.of()));
                        try {
                            start.await();
                            bean(AssignRolesToUserUseCase.class)
                                    .execute(
                                            new AssignRolesToUserCommand(
                                                    u.uuid(), List.of(r.uuid()), null, "Concurrent"));
                            return true;
                        } catch (DuplicateResourceException duplicate) {
                            return false;
                        } finally {
                            SecurityContextHolder.clearContext();
                        }
                    };
            var first = executor.submit(task);
            var second = executor.submit(task);
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        }
    }
}
