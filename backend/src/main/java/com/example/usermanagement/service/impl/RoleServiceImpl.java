package com.example.usermanagement.service.impl;

import com.example.usermanagement.dto.PermissionDTO;
import com.example.usermanagement.dto.RoleDTO;
import com.example.usermanagement.entity.Permission;
import com.example.usermanagement.entity.Role;
import com.example.usermanagement.exception.BusinessException;
import com.example.usermanagement.exception.ResourceNotFoundException;
import com.example.usermanagement.repository.PermissionRepository;
import com.example.usermanagement.repository.RoleRepository;
import com.example.usermanagement.service.RoleService;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public RoleDTO.Response createRole(RoleDTO.CreateRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new BusinessException("ROLE_EXISTS", "Role name already exists");
        }

        Role role = Role.builder()
                .name(request.getName().toUpperCase())
                .description(request.getDescription())
                .defaultRole(request.isDefaultRole())
                .build();

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(
                    permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        return mapToResponse(savedRole);
    }

    @Override
    @Transactional
    public RoleDTO.Response updateRole(UUID id, RoleDTO.UpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        if (request.getDefaultRole() != null) {
            // If setting this role as default, remove default from others
            if (request.getDefaultRole() && !role.isDefaultRole()) {
                roleRepository.findByDefaultRoleTrue().ifPresent(defaultRole -> {
                    defaultRole.setDefaultRole(false);
                    roleRepository.save(defaultRole);
                });
            }
            role.setDefaultRole(request.getDefaultRole());
        }

        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(
                    permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(permissions);
        }

        Role updatedRole = roleRepository.save(role);
        return mapToResponse(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (role.isDefaultRole()) {
            throw new BusinessException("CANNOT_DELETE_DEFAULT",
                    "Cannot delete the default role");
        }

        roleRepository.delete(role);
    }

    @Override
    public RoleDTO.Response getRoleById(UUID id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return mapToResponse(role);
    }

    @Override
    public RoleDTO.Response getRoleByName(String name) {
        Role role = roleRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
        return mapToResponse(role);
    }

    @Override
    public Page<RoleDTO.Response> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void assignPermissionToRole(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        role.addPermission(permission);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        role.removePermission(permission);
        roleRepository.save(role);
    }

    private RoleDTO.Response mapToResponse(Role role) {
        return RoleDTO.Response.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .defaultRole(role.isDefaultRole())
                .createdAt(role.getCreatedAt())
                .permissions(role.getPermissions().stream()
                        .map(permission -> PermissionDTO.Response.builder()
                                .id(permission.getId())
                                .name(permission.getName())
                                .resource(permission.getResource())
                                .action(permission.getAction())
                                .createdAt(permission.getCreatedAt())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
