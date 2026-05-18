package com.zhhs.nong.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record PermissionRequest(
        @NotBlank(message = "module is required") String module,
        @NotBlank(message = "action is required") String action,
        @NotBlank(message = "role is required") String role
) {}
