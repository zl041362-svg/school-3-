package com.zhhs.nong.dto.admin;

import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull(message = "cannot be null") Boolean approved,
        String reason
) {
}

