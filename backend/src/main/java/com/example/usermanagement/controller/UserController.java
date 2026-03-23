package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.UserDTO;
import com.example.usermanagement.security.UserPrincipal;
import com.example.usermanagement.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read') or hasRole('ADMIN')")
    public ApiResponse<Page<UserDTO.Response>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<UserDTO.Response> users = search != null && !search.isEmpty()
                ? userService.searchUsers(search, pageable)
                : userService.getAllUsers(pageable);
        return ApiResponse.success(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read') or hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public ApiResponse<UserDTO.Response> getUserById(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDTO.Response> createUser(
            @Valid @RequestBody UserDTO.CreateRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update') or hasRole('ADMIN') or @securityService.isCurrentUser(#id, authentication)")
    public ApiResponse<UserDTO.Response> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO.UpdateRequest request) {
        return ApiResponse.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAuthority('user:update') or hasRole('ADMIN')")
    public ApiResponse<Void> assignRole(
            @PathVariable UUID id,
            @PathVariable UUID roleId) {
        userService.assignRoleToUser(id, roleId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAuthority('user:update') or hasRole('ADMIN')")
    public ApiResponse<Void> removeRole(
            @PathVariable UUID id,
            @PathVariable UUID roleId) {
        userService.removeRoleFromUser(id, roleId);
        return ApiResponse.success(null);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserDTO.ChangePasswordRequest request) {
        userService.changePassword(userPrincipal.getId(), request);
        return ApiResponse.success(null);
    }
}
