package com.zhhs.nong.dto.trade;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank(message = "cannot be blank") String receiver,
        @NotBlank(message = "cannot be blank") String phone,
        @NotBlank(message = "cannot be blank") String address
) {
}
