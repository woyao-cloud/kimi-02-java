package com.example.usermanagement.service.impl;

import com.example.usermanagement.dto.PermissionDTO;
import com.example.usermanagement.entity.Permission;
import com.example.usermanagement.exception.BusinessException;
import com.example.usermanagement.exception.ResourceNotFoundException;
import com.example.usermanagement.repository.PermissionRepository;
import com.example.usermanagement.service.PermissionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public PermissionDTO.Response createPermission(PermissionDTO.CreateRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new BusinessException("PERMISSION_EXISTS", "Permission name already exists");
        }

        Permission permission = Permission.builder()
                .name(request.getName().toLowerCase())
                .resource(request.getResource().toLowerCase())
                .action(request.getAction().toLowerCase())
                .build();

        Permission savedPermission = permissionRepository.save(permission);
        return mapToResponse(savedPermission);
    }

    @Override
    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        permissionRepository.delete(permission);
    }

    @Override
    public PermissionDTO.Response getPermissionById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        return mapToResponse(permission);
    }

    @Override
    public PermissionDTO.Response getPermissionByName(String name) {
        Permission permission = permissionRepository.findByName(name.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "name", name));
        return mapToResponse(permission);
    }

    @Override
    public Page<PermissionDTO.Response> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(this::mapToResponse);
    }

    private PermissionDTO.Response mapToResponse(Permission permission) {
        return PermissionDTO.Response.builder()
                .id(permission.getId())
                .name(permission.getName())
                .resource(permission.getResource())
                .action(permission.getAction())
                .createdAt(permission.getCreatedAt())
                .build();
    }
}
