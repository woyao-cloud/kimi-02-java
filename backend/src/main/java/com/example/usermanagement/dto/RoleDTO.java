package com.example.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

public class RoleDTO {

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private UUID id;
        private String name;
        private String description;
        private boolean defaultRole;
        private LocalDateTime createdAt;
        private Set<PermissionDTO.Response> permissions;
    }

    @Data
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Role name is required")
        @Size(max = 50, message = "Role name must not exceed 50 characters")
        private String name;

        private String description;

        private boolean defaultRole;

        private Set<UUID> permissionIds;
    }

    @Data
    @Builder
    public static class UpdateRequest {
        private String description;

        private Boolean defaultRole;

        private Set<UUID> permissionIds;
    }
}
