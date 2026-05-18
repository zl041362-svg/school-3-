package com.zhhs.nong.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record SaveNewsAdminRequest(
        @NotBlank(message = "title is required") String title,
        String category,
        String summary,
        String content,
        String author,
        String status
) {}
