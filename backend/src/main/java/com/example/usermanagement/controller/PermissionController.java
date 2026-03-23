package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.PermissionDTO;
import com.example.usermanagement.service.PermissionService;
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
@RequestMapping("/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('permission:read') or hasRole('ADMIN')")
    public ApiResponse<Page<PermissionDTO.Response>> getAllPermissions(
            @PageableDefault(size = 50) Pageable pageable) {
        return ApiResponse.success(permissionService.getAllPermissions(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:read') or hasRole('ADMIN')")
    public ApiResponse<PermissionDTO.Response> getPermissionById(@PathVariable UUID id) {
        return ApiResponse.success(permissionService.getPermissionById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('permission:create') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PermissionDTO.Response> createPermission(
            @Valid @RequestBody PermissionDTO.CreateRequest request) {
        return ApiResponse.success(permissionService.createPermission(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deletePermission(@PathVariable UUID id) {
        permissionService.deletePermission(id);
        return ApiResponse.success(null);
    }
}
