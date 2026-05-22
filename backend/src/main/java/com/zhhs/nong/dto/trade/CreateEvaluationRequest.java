package com.zhhs.nong.dto.trade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateEvaluationRequest(
        @Min(1) @Max(5) int rating,
        String content
) {}
