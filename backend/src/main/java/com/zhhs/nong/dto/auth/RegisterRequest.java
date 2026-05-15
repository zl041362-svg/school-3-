package com.zhhs.nong.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "cannot be blank") String phone,
        @NotBlank(message = "cannot be blank") String password,
        @Pattern(regexp = "customer|farmer", message = "must be customer or farmer") String role
) {
}

