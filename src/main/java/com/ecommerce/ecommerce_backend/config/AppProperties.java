package com.ecommerce.ecommerce_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// Registers "app.*" as a known property namespace
// This eliminates the "Unknown property 'app'" warning in VSCode/IntelliJ
// and gives you type-safe access to custom properties anywhere in the app
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs;
    }
}