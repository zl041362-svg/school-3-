package com.zhhs.nong.common;

import org.springframework.security.core.Authentication;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id(Authentication authentication) {
        return Long.parseLong(String.valueOf(authentication.getPrincipal()));
    }

    public static String operator(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return "system";
        }
        return authentication.getAuthorities().iterator().next().getAuthority()
                .replace("ROLE_", "")
                .toLowerCase();
    }
}
