package com.airesume.analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleChangeRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "New role is required")
    private String newRole; // ROLE_USER, ROLE_RECRUITER, ROLE_ADMIN
}
