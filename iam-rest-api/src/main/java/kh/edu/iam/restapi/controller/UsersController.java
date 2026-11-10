package kh.edu.iam.restapi.controller;

import jakarta.validation.Valid;

import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.identity.usecase.ListUserIdentitiesUseCase;
import kh.edu.iam.application.identity.usecase.UnlinkIdentityUseCase;
import kh.edu.iam.application.user.command.AssignRolesToUserCommand;
import kh.edu.iam.application.user.command.GrantPermissionsToUserCommand;
import kh.edu.iam.application.user.query.UserFilterQuery;
import kh.edu.iam.application.user.result.PermissionGrantResult;
import kh.edu.iam.application.user.result.RoleAssignmentResult;
import kh.edu.iam.application.user.result.UserSummary;
import kh.edu.iam.application.user.usecase.AssignRolesToUserUseCase;
import kh.edu.iam.application.user.usecase.CreateUserUseCase;
import kh.edu.iam.application.user.usecase.DeleteUserUseCase;
import kh.edu.iam.application.user.usecase.DisableUserUseCase;
import kh.edu.iam.application.user.usecase.EnableUserUseCase;
import kh.edu.iam.application.user.usecase.GetUserAuthoritiesUseCase;
import kh.edu.iam.application.user.usecase.GetUserUseCase;
import kh.edu.iam.application.user.usecase.GrantPermissionsToUserUseCase;
import kh.edu.iam.application.user.usecase.ListUsersUseCase;
import kh.edu.iam.application.user.usecase.LockUserUseCase;
import kh.edu.iam.application.user.usecase.ResetUserPasswordUseCase;
import kh.edu.iam.application.user.usecase.RevokePermissionFromUserUseCase;
import kh.edu.iam.application.user.usecase.RevokeRoleFromUserUseCase;
import kh.edu.iam.application.user.usecase.UnlockUserUseCase;
import kh.edu.iam.application.user.usecase.UpdateUserProfileUseCase;
import kh.edu.iam.restapi.dto.ProfileRequest;
import kh.edu.iam.restapi.dto.Requests;
import kh.edu.iam.restapi.dto.Responses;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@io.swagger.v3.oas.annotations.tags.Tag(name="Users", description="Manage user accounts and access.")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {
    private final CreateUserUseCase createUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final EnableUserUseCase enableUserUseCase;
    private final DisableUserUseCase disableUserUseCase;
    private final LockUserUseCase lockUserUseCase;
    private final UnlockUserUseCase unlockUserUseCase;
    private final ResetUserPasswordUseCase resetUserPasswordUseCase;
    private final GetUserAuthoritiesUseCase getUserAuthoritiesUseCase;
    private final AssignRolesToUserUseCase assignRolesToUserUseCase;
    private final GrantPermissionsToUserUseCase grantPermissionsToUserUseCase;
    private final RevokeRoleFromUserUseCase revokeRoleFromUserUseCase;
    private final RevokePermissionFromUserUseCase revokePermissionFromUserUseCase;
    private final ListUserIdentitiesUseCase listUserIdentitiesUseCase;
    private final UnlinkIdentityUseCase unlinkIdentityUseCase;

    @io.swagger.v3.oas.annotations.Operation(summary="Create user")
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.User create(@Valid @RequestBody Requests.CreateUser request) {
        return Responses.user(createUserUseCase.execute(request.command()));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="List users")
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public PageResult<UserSummary> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "enabled", required = false) Boolean enabled,
            @RequestParam(name = "locked", required = false) Boolean locked) {
        return listUsersUseCase.execute(new UserFilterQuery(page, size, sort, search, enabled, locked));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Get user")
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('user:read')")
    public Responses.User get(@PathVariable("uuid") UUID uuid) {
        return Responses.user(getUserUseCase.execute(uuid));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Update user")
    @PatchMapping("/{uuid}")
    @PreAuthorize("hasAuthority('user:update')")
    public Responses.User update(@PathVariable("uuid") UUID uuid, @Valid @RequestBody ProfileRequest request) {
        return Responses.user(updateUserProfileUseCase.execute(request.command(uuid)));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Delete user")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('user:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("uuid") UUID uuid) {
        deleteUserUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Enable user")
    @PostMapping("/{uuid}/enable")
    @PreAuthorize("hasAuthority('user:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable("uuid") UUID uuid) {
        enableUserUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Disable user")
    @PostMapping("/{uuid}/disable")
    @PreAuthorize("hasAuthority('user:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable("uuid") UUID uuid) {
        disableUserUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Lock user")
    @PostMapping("/{uuid}/lock")
    @PreAuthorize("hasAuthority('user:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void lock(@PathVariable("uuid") UUID uuid) {
        lockUserUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Unlock user")
    @PostMapping("/{uuid}/unlock")
    @PreAuthorize("hasAuthority('user:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlock(@PathVariable("uuid") UUID uuid) {
        unlockUserUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Reset user password")
    @PutMapping("/{uuid}/password")
    @PreAuthorize("hasAuthority('user:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@PathVariable("uuid") UUID uuid, @Valid @RequestBody Requests.ResetPassword request) {
        resetUserPasswordUseCase.execute(request.command(uuid));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Get user permissions and roles")
    @GetMapping("/{uuid}/authorities")
    @PreAuthorize("hasAuthority('user:read')")
    public Responses.Authorities authorities(@PathVariable("uuid") UUID uuid) {
        return Responses.authorities(getUserAuthoritiesUseCase.execute(uuid));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Assign roles to user")
    @PostMapping("/{uuid}/roles")
    @PreAuthorize("hasAuthority('user:assign')")
    public List<RoleAssignmentResult> assignRoles(
            @PathVariable("uuid") UUID uuid, @Valid @RequestBody Requests.AssignRoles request) {
        return assignRolesToUserUseCase.execute(
                new AssignRolesToUserCommand(uuid, request.roleUuids(), request.expiresAt(), request.reason()));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Grant user permissions")
    @PostMapping("/{uuid}/permissions")
    @PreAuthorize("hasAuthority('user:assign')")
    public List<PermissionGrantResult> grantPermissions(
            @PathVariable("uuid") UUID uuid, @Valid @RequestBody Requests.GrantPermissions request) {
        return grantPermissionsToUserUseCase.execute(
                new GrantPermissionsToUserCommand(
                        uuid, request.permissionUuids(), request.effect(), request.expiresAt(), request.reason()));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Remove user role")
    @DeleteMapping("/{uuid}/roles/{roleUuid}")
    @PreAuthorize("hasAuthority('user:assign')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeRoles(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("roleUuid") UUID roleUuid,
            @Valid @RequestBody Requests.Revoke request) {
        revokeRoleFromUserUseCase.execute(uuid, roleUuid, request.reason());
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Revoke user permission")
    @DeleteMapping("/{uuid}/permissions/{permissionUuid}")
    @PreAuthorize("hasAuthority('user:assign')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokePermissions(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("permissionUuid") UUID permissionUuid,
            @Valid @RequestBody Requests.Revoke request) {
        revokePermissionFromUserUseCase.execute(uuid, permissionUuid, request.reason());
    }

    @io.swagger.v3.oas.annotations.Operation(summary="List user sign-in methods")
    @GetMapping("/{uuid}/identities")
    @PreAuthorize("hasAuthority('user:read')")
    public List<Responses.Identity> identities(@PathVariable("uuid") UUID uuid) {
        return listUserIdentitiesUseCase.execute(uuid).stream().map(Responses::identity).toList();
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Unlink user sign-in method")
    @DeleteMapping("/{uuid}/identities/{identityUuid}")
    @PreAuthorize("hasAuthority('user:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlink(@PathVariable("uuid") UUID uuid, @PathVariable("identityUuid") UUID identityUuid) {
        unlinkIdentityUseCase.execute(uuid, identityUuid);
    }
}
