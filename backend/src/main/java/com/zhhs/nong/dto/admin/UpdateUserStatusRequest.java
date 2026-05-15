package com.zhhs.nong.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserStatusRequest(
        @NotBlank(message = "cannot be blank")
        @Pattern(regexp = "active|disabled", message = "must be active or disabled") String status
) {
}

