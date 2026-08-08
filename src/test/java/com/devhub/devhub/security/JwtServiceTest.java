package com.devhub.devhub.security;

import com.devhub.devhub.security.config.JwtProperties;
import com.devhub.devhub.security.jwt.JwtService;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    private static final String USERNAME = "test@example.com";

    /*
     * HS512 requires at least 64 decoded bytes.
     *
     * These are test-only secrets.
     * They are deliberately generated as Base64 strings because
     * JwtService expects Base64-encoded secrets.
     */
    private static final String ACCESS_SECRET = base64Secret(
            "access-secret-for-jwt-tests-must-be-at-least-64-bytes-long-123456"
    );

    private static final String REFRESH_SECRET = base64Secret(
            "refresh-secret-for-jwt-tests-must-be-at-least-64-bytes-long-654321"
    );

    private static final long ACCESS_EXPIRATION = 15 * 60 * 1000L; // 15 minutes
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 days

    @BeforeEach
    void setUp() {
        JwtProperties.Token accessToken = new JwtProperties.Token(
                ACCESS_SECRET,
                ACCESS_EXPIRATION
        );

        JwtProperties.Token refreshToken = new JwtProperties.Token(
                REFRESH_SECRET,
                REFRESH_EXPIRATION
        );

        JwtProperties properties = new JwtProperties(
                accessToken,
                refreshToken
        );

        jwtService = new JwtService(properties);

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);
    }

    @Test
    void shouldGenerateAccessToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsernameFromAccessToken() {
        String token = jwtService.generateAccessToken(userDetails);

        String username = jwtService.extractUsername(token, false);

        assertEquals(USERNAME, username);
    }

    @Test
    void shouldExtractUsernameFromRefreshToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        String username = jwtService.extractUsername(token, true);

        assertEquals(USERNAME, username);
    }

    @Test
    void shouldValidateAccessToken() {
        String token = jwtService.generateAccessToken(userDetails);

        boolean valid = jwtService.isTokenValid(
                token,
                userDetails,
                false
        );

        assertTrue(valid);
    }

    @Test
    void shouldValidateRefreshToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        boolean valid = jwtService.isTokenValid(
                token,
                userDetails,
                true
        );

        assertTrue(valid);
    }

    @Test
    void shouldRejectAccessTokenForDifferentUser() {
        String token = jwtService.generateAccessToken(userDetails);

        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername())
                .thenReturn("different@example.com");

        boolean valid = jwtService.isTokenValid(
                token,
                differentUser,
                false
        );

        assertFalse(valid);
    }

    @Test
    void shouldRejectRefreshTokenForDifferentUser() {
        String token = jwtService.generateRefreshToken(userDetails);

        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername())
                .thenReturn("different@example.com");

        boolean valid = jwtService.isTokenValid(
                token,
                differentUser,
                true
        );

        assertFalse(valid);
    }

    @Test
    void shouldRejectAccessTokenParsedWithRefreshSecret() {
        String accessToken = jwtService.generateAccessToken(userDetails);

        assertThrows(
                SignatureException.class,
                () -> jwtService.extractUsername(accessToken, true)
        );
    }

    @Test
    void shouldRejectRefreshTokenParsedWithAccessSecret() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        assertThrows(
                SignatureException.class,
                () -> jwtService.extractUsername(refreshToken, false)
        );
    }

    @Test
    void shouldRejectExpiredAccessToken() {
        JwtProperties.Token expiredAccessToken = new JwtProperties.Token(
                ACCESS_SECRET,
                -1000L
        );

        JwtProperties.Token refreshToken = new JwtProperties.Token(
                REFRESH_SECRET,
                REFRESH_EXPIRATION
        );

        JwtProperties properties = new JwtProperties(
                expiredAccessToken,
                refreshToken
        );

        JwtService expiredJwtService = new JwtService(properties);

        String token = expiredJwtService.generateAccessToken(userDetails);

        assertFalse(
                expiredJwtService.isTokenValid(
                        token,
                        userDetails,
                        false
                )
        );
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        JwtProperties.Token accessToken = new JwtProperties.Token(
                ACCESS_SECRET,
                ACCESS_EXPIRATION
        );

        JwtProperties.Token expiredRefreshToken = new JwtProperties.Token(
                REFRESH_SECRET,
                -1000L
        );

        JwtProperties properties = new JwtProperties(
                accessToken,
                expiredRefreshToken
        );

        JwtService expiredJwtService = new JwtService(properties);

        String token = expiredJwtService.generateRefreshToken(userDetails);

        assertFalse(
                expiredJwtService.isTokenValid(
                        token,
                        userDetails,
                        true
                )
        );
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThrows(
                Exception.class,
                () -> jwtService.extractUsername(
                        "this-is-not-a-jwt",
                        false
                )
        );
    }

    private static String base64Secret(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 64) {
            throw new IllegalArgumentException(
                    "Test secret must contain at least 64 bytes"
            );
        }

        return Base64.getEncoder().encodeToString(bytes);
    }
}