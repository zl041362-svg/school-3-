package com.zhhs.nong.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleMembersRequest(
        @NotNull(message = "cannot be null") @Min(value = 0, message = "must be at least 0") Integer members
) {
}

