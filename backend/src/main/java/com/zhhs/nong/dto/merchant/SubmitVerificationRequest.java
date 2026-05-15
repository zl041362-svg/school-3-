package com.zhhs.nong.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitVerificationRequest(
        @NotBlank(message = "realName is required") String realName,
        @NotBlank(message = "idNumber is required") @Size(min = 18, max = 18, message = "idNumber must be 18 characters") String idNumber,
        @NotBlank(message = "businessNo is required") @Size(min = 15, max = 18, message = "businessNo must be 15-18 characters") String businessNo
) {}
