package com.example.usermanagement.service;

import com.example.usermanagement.dto.PermissionDTO;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionService {

    PermissionDTO.Response createPermission(PermissionDTO.CreateRequest request);

    void deletePermission(UUID id);

    PermissionDTO.Response getPermissionById(UUID id);

    PermissionDTO.Response getPermissionByName(String name);

    Page<PermissionDTO.Response> getAllPermissions(Pageable pageable);
}
