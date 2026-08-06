package com.devhub.devhub.auth.dto;

public record AuthResponse(

        String accessToken,

        String refreshToken,

        String tokenType

) {
}
