package com.zhhs.nong.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "cannot be blank") String phone,
        @NotBlank(message = "cannot be blank") @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$", message = "password must be 8-20 chars with letters and digits") String password,
        @Pattern(regexp = "customer|farmer", message = "must be customer or farmer") String role
) {
}

