package kh.edu.iam.restapi.controller;

import jakarta.validation.Valid;

import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.role.command.AssignPermissionsToRoleCommand;
import kh.edu.iam.application.role.query.RoleFilterQuery;
import kh.edu.iam.application.role.result.PermissionAssignmentResult;
import kh.edu.iam.application.role.result.RoleResult;
import kh.edu.iam.application.role.result.RoleSummary;
import kh.edu.iam.application.role.usecase.ActivateRoleUseCase;
import kh.edu.iam.application.role.usecase.AssignPermissionsToRoleUseCase;
import kh.edu.iam.application.role.usecase.CreateRoleUseCase;
import kh.edu.iam.application.role.usecase.DeactivateRoleUseCase;
import kh.edu.iam.application.role.usecase.DeleteRoleUseCase;
import kh.edu.iam.application.role.usecase.GetRoleUseCase;
import kh.edu.iam.application.role.usecase.ListRolesUseCase;
import kh.edu.iam.application.role.usecase.RevokePermissionFromRoleUseCase;
import kh.edu.iam.application.role.usecase.UpdateRoleUseCase;
import kh.edu.iam.restapi.dto.Requests;
import kh.edu.iam.restapi.dto.RolePatchRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@io.swagger.v3.oas.annotations.tags.Tag(name="Roles", description="Manage roles and their permissions.")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolesController {
    private final CreateRoleUseCase createRoleUseCase;
    private final ListRolesUseCase listRolesUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final ActivateRoleUseCase activateRoleUseCase;
    private final DeactivateRoleUseCase deactivateRoleUseCase;
    private final AssignPermissionsToRoleUseCase assignPermissionsToRoleUseCase;
    private final RevokePermissionFromRoleUseCase revokePermissionFromRoleUseCase;

    @io.swagger.v3.oas.annotations.Operation(summary="Create role")
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResult create(@Valid @RequestBody Requests.CreateRole request) {
        return createRoleUseCase.execute(request.command());
    }

    @io.swagger.v3.oas.annotations.Operation(summary="List roles")
    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    public PageResult<RoleSummary> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "active", required = false) Boolean active) {
        return listRolesUseCase.execute(new RoleFilterQuery(page, size, sort, search, active));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Get role")
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('role:read')")
    public RoleResult get(@PathVariable("uuid") UUID uuid) {
        return getRoleUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Update role")
    @PatchMapping("/{uuid}")
    @PreAuthorize("hasAuthority('role:update')")
    public RoleResult update(@PathVariable("uuid") UUID uuid, @Valid @RequestBody RolePatchRequest request) {
        return updateRoleUseCase.execute(request.command(uuid));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Delete role")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('role:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("uuid") UUID uuid) {
        deleteRoleUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Activate role")
    @PostMapping("/{uuid}/activate")
    @PreAuthorize("hasAuthority('role:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable("uuid") UUID uuid) {
        activateRoleUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Deactivate role")
    @PostMapping("/{uuid}/deactivate")
    @PreAuthorize("hasAuthority('role:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable("uuid") UUID uuid) {
        deactivateRoleUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Assign permissions to role")
    @PostMapping("/{uuid}/permissions")
    @PreAuthorize("hasAuthority('role:assign')")
    public List<PermissionAssignmentResult> assignPermissions(
            @PathVariable("uuid") UUID uuid, @Valid @RequestBody Requests.AssignPermissions request) {
        return assignPermissionsToRoleUseCase.execute(
                new AssignPermissionsToRoleCommand(
                        uuid, request.permissionUuids(), request.expiresAt(), request.reason()));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Remove role permission")
    @DeleteMapping("/{uuid}/permissions/{permissionUuid}")
    @PreAuthorize("hasAuthority('role:assign')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokePermission(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("permissionUuid") UUID permissionUuid,
            @Valid @RequestBody Requests.Revoke request) {
        revokePermissionFromRoleUseCase.execute(uuid, permissionUuid, request.reason());
    }
}
