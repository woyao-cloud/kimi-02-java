package com.example.usermanagement.service;

import com.example.usermanagement.dto.AuthDTO;
import com.example.usermanagement.dto.UserDTO;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDTO.Response createUser(UserDTO.CreateRequest request);

    UserDTO.Response updateUser(UUID id, UserDTO.UpdateRequest request);

    void deleteUser(UUID id);

    UserDTO.Response getUserById(UUID id);

    UserDTO.Response getUserByUsername(String username);

    Page<UserDTO.Response> getAllUsers(Pageable pageable);

    Page<UserDTO.Response> searchUsers(String search, Pageable pageable);

    void changePassword(UUID userId, UserDTO.ChangePasswordRequest request);

    void assignRoleToUser(UUID userId, UUID roleId);

    void removeRoleFromUser(UUID userId, UUID roleId);

    AuthDTO.CurrentUserResponse getCurrentUser(UUID userId);
}
