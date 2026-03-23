package com.example.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

public class UserDTO {

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private UUID id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private boolean active;
        private boolean verified;
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Set<RoleDTO.Response> roles;
    }

    @Data
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @Size(max = 100, message = "First name must not exceed 100 characters")
        private String firstName;

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        private String lastName;

        private Set<UUID> roleIds;
    }

    @Data
    @Builder
    public static class UpdateRequest {
        @Email(message = "Email must be valid")
        private String email;

        @Size(max = 100, message = "First name must not exceed 100 characters")
        private String firstName;

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        private String lastName;

        private Boolean active;

        private Set<UUID> roleIds;
    }

    @Data
    @Builder
    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        private String newPassword;
    }
}
