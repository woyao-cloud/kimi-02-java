package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.RoleDTO;
import com.example.usermanagement.service.RoleService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('role:read') or hasRole('ADMIN')")
    public ApiResponse<Page<RoleDTO.Response>> getAllRoles(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(roleService.getAllRoles(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:read') or hasRole('ADMIN')")
    public ApiResponse<RoleDTO.Response> getRoleById(@PathVariable UUID id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:create') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoleDTO.Response> createRole(
            @Valid @RequestBody RoleDTO.CreateRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update') or hasRole('ADMIN')")
    public ApiResponse<RoleDTO.Response> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleDTO.UpdateRequest request) {
        return ApiResponse.success(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:update') or hasRole('ADMIN')")
    public ApiResponse<Void> assignPermission(
            @PathVariable UUID id,
            @PathVariable UUID permissionId) {
        roleService.assignPermissionToRole(id, permissionId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:update') or hasRole('ADMIN')")
    public ApiResponse<Void> removePermission(
            @PathVariable UUID id,
            @PathVariable UUID permissionId) {
        roleService.removePermissionFromRole(id, permissionId);
        return ApiResponse.success(null);
    }
}
