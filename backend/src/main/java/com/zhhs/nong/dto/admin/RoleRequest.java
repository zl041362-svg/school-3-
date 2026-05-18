package com.zhhs.nong.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
        @NotBlank(message = "role is required") String role,
        String description
) {}
