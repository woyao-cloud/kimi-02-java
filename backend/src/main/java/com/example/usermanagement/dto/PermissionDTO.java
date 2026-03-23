package com.example.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

public class PermissionDTO {

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private UUID id;
        private String name;
        private String resource;
        private String action;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Permission name is required")
        @Size(max = 100, message = "Permission name must not exceed 100 characters")
        private String name;

        @NotBlank(message = "Resource is required")
        @Size(max = 50, message = "Resource must not exceed 50 characters")
        private String resource;

        @NotBlank(message = "Action is required")
        @Size(max = 50, message = "Action must not exceed 50 characters")
        private String action;
    }
}
