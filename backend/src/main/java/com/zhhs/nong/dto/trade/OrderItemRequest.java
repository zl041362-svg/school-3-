package com.zhhs.nong.dto.trade;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "cannot be null") Long productId,
        @NotNull(message = "cannot be null") @Min(value = 1, message = "must be at least 1") Integer qty
) {
}

