package com.zhhs.nong.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "change-me-to-a-safe-secret-change-me-to-a-safe-secret";
    private long expirationSeconds = 604800;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }
}

