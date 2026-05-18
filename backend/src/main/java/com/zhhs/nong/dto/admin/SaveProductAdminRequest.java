package com.zhhs.nong.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SaveProductAdminRequest(
        @NotBlank(message = "name is required") String name,
        String category,
        String region,
        @NotNull(message = "price is required") BigDecimal price,
        @NotNull(message = "stock is required") Integer stock,
        String summary,
        String description,
        String spec,
        String qualification,
        String farmer,
        String status
) {}
