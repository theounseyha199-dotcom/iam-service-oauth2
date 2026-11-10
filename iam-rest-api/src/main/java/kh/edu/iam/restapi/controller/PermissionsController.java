package kh.edu.iam.restapi.controller;

import jakarta.validation.Valid;

import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.permission.query.PermissionFilterQuery;
import kh.edu.iam.application.permission.result.PermissionResult;
import kh.edu.iam.application.permission.usecase.ActivatePermissionUseCase;
import kh.edu.iam.application.permission.usecase.CreatePermissionUseCase;
import kh.edu.iam.application.permission.usecase.DeactivatePermissionUseCase;
import kh.edu.iam.application.permission.usecase.DeletePermissionUseCase;
import kh.edu.iam.application.permission.usecase.GetPermissionUseCase;
import kh.edu.iam.application.permission.usecase.ListPermissionsUseCase;
import kh.edu.iam.application.permission.usecase.UpdatePermissionUseCase;
import kh.edu.iam.restapi.dto.PermissionPatchRequest;
import kh.edu.iam.restapi.dto.Requests;

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

import java.util.UUID;

@io.swagger.v3.oas.annotations.tags.Tag(name="Permissions", description="Manage available permissions.")
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionsController {
    private final CreatePermissionUseCase createPermissionUseCase;
    private final ListPermissionsUseCase listPermissionsUseCase;
    private final GetPermissionUseCase getPermissionUseCase;
    private final UpdatePermissionUseCase updatePermissionUseCase;
    private final DeletePermissionUseCase deletePermissionUseCase;
    private final ActivatePermissionUseCase activatePermissionUseCase;
    private final DeactivatePermissionUseCase deactivatePermissionUseCase;

    @io.swagger.v3.oas.annotations.Operation(summary="Create permission")
    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResult create(@Valid @RequestBody Requests.CreatePermission request) {
        return createPermissionUseCase.execute(request.command());
    }

    @io.swagger.v3.oas.annotations.Operation(summary="List permissions")
    @GetMapping
    @PreAuthorize("hasAuthority('permission:read')")
    public PageResult<PermissionResult> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "resource", required = false) String resource,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "active", required = false) Boolean active) {
        return listPermissionsUseCase.execute(
                new PermissionFilterQuery(page, size, sort, search, resource, action, active));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Get permission")
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('permission:read')")
    public PermissionResult get(@PathVariable("uuid") UUID uuid) {
        return getPermissionUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Update permission")
    @PatchMapping("/{uuid}")
    @PreAuthorize("hasAuthority('permission:update')")
    public PermissionResult update(
            @PathVariable("uuid") UUID uuid, @Valid @RequestBody PermissionPatchRequest request) {
        return updatePermissionUseCase.execute(request.command(uuid));
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Delete permission")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('permission:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("uuid") UUID uuid) {
        deletePermissionUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Activate permission")
    @PostMapping("/{uuid}/activate")
    @PreAuthorize("hasAuthority('permission:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable("uuid") UUID uuid) {
        activatePermissionUseCase.execute(uuid);
    }

    @io.swagger.v3.oas.annotations.Operation(summary="Deactivate permission")
    @PostMapping("/{uuid}/deactivate")
    @PreAuthorize("hasAuthority('permission:update')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable("uuid") UUID uuid) {
        deactivatePermissionUseCase.execute(uuid);
    }
}
