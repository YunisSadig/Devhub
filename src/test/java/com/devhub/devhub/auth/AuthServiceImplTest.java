package com.devhub.devhub.auth;

import com.devhub.devhub.auth.dto.AuthResponse;
import com.devhub.devhub.auth.dto.LoginRequest;
import com.devhub.devhub.auth.dto.RegisterRequest;
import com.devhub.devhub.common.EmailAlreadyExistsException;
import com.devhub.devhub.common.ResourceNotFoundException;
import com.devhub.devhub.security.jwt.JwtService;
import com.devhub.devhub.security.refresh.RefreshToken;
import com.devhub.devhub.security.refresh.RefreshTokenService;
import com.devhub.devhub.user.User;
import com.devhub.devhub.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        loginRequest = new LoginRequest(
                "john@example.com",
                "password123"
        );
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        // Arrange
        User savedUser = new User(
                "John",
                "Doe",
                "john@example.com",
                "encodedPassword",
                null
        );

        RefreshToken refreshToken = mock(RefreshToken.class);

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        when(jwtService.generateAccessToken(any(UserDetails.class)))
                .thenReturn("access-token");

        when(refreshTokenService.create(savedUser))
                .thenReturn(refreshToken);

        when(refreshToken.getToken())
                .thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("Bearer", response.tokenType());

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateAccessToken(any(UserDetails.class));
        verify(refreshTokenService).create(savedUser);
        verify(refreshToken).getToken();
    }

    @Test
    void register_shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(registerRequest)
        );

        verify(userRepository).existsByEmail("john@example.com");

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void login_shouldAuthenticateUserAndReturnTokens() {
        // Arrange
        User user = new User(
                "John",
                "Doe",
                "john@example.com",
                "encodedPassword",
                null
        );

        RefreshToken refreshToken = mock(RefreshToken.class);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(any(UserDetails.class)))
                .thenReturn("access-token");

        when(refreshTokenService.create(user))
                .thenReturn(refreshToken);

        when(refreshToken.getToken())
                .thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("Bearer", response.tokenType());

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(userRepository).findByEmail("john@example.com");
        verify(jwtService).generateAccessToken(any(UserDetails.class));
        verify(refreshTokenService).create(user);
        verify(refreshToken).getToken();
    }

    @Test
    void login_shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.login(loginRequest)
        );

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(userRepository).findByEmail("john@example.com");

        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void logout_shouldRevokeRefreshToken() {
        // Arrange
        String refreshToken = "refresh-token";

        // Act
        authService.logout(refreshToken);

        // Assert
        verify(refreshTokenService).revoke(refreshToken);
    }
}