package com.devhub.devhub.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        Token access,

        Token refresh

) {

    public record Token(

            String secret,

            long expiration

    ) {
    }
}