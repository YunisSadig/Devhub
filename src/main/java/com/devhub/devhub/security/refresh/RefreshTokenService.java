package com.devhub.devhub.security.refresh;

import com.devhub.devhub.security.jwt.JwtService;
import com.devhub.devhub.security.user.CustomUserDetailsService;
import com.devhub.devhub.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public RefreshToken create(User user) {

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());

        String token = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshToken = new RefreshToken(
                token,
                user,
                LocalDateTime.now().plusSeconds(
                        jwtService.getRefreshTokenExpiration() / 1000
                )
        );

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validate(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid refresh token")
                );

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid refresh token")
                );

        refreshToken.revoke();

        refreshTokenRepository.save(refreshToken);
    }
}