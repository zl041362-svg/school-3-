package com.zhhs.nong.dto.merchant;

import jakarta.validation.constraints.NotBlank;

public record SaveNewsRequest(
        @NotBlank(message = "title is required") String title,
        @NotBlank(message = "category is required") String category,
        String summary,
        @NotBlank(message = "content is required") String content
) {}
