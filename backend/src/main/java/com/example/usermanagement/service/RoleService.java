package com.example.usermanagement.service;

import com.example.usermanagement.dto.RoleDTO;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {

    RoleDTO.Response createRole(RoleDTO.CreateRequest request);

    RoleDTO.Response updateRole(UUID id, RoleDTO.UpdateRequest request);

    void deleteRole(UUID id);

    RoleDTO.Response getRoleById(UUID id);

    RoleDTO.Response getRoleByName(String name);

    Page<RoleDTO.Response> getAllRoles(Pageable pageable);

    void assignPermissionToRole(UUID roleId, UUID permissionId);

    void removePermissionFromRole(UUID roleId, UUID permissionId);
}
