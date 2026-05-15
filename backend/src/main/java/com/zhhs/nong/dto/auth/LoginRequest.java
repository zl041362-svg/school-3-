package com.zhhs.nong.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "cannot be blank") String phone,
        @NotBlank(message = "cannot be blank") String password
) {
}

