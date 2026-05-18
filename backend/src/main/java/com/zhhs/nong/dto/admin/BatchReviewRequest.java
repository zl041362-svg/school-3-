package com.zhhs.nong.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BatchReviewRequest(
        @NotEmpty(message = "ids cannot be empty") List<Long> ids,
        @NotNull(message = "approved cannot be null") Boolean approved,
        String reason
) {}
