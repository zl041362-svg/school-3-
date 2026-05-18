package com.zhhs.nong.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record StatusRequest(
        @NotBlank(message = "status is required") String status
) {}
