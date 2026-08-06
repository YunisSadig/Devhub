package com.devhub.devhub.security.jwt;

import com.devhub.devhub.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "ACCESS");

        return generateToken(
                claims,
                userDetails,
                jwtProperties.access().expiration(),
                jwtProperties.access().secret()
        );
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");

        return generateToken(
                claims,
                userDetails,
                jwtProperties.refresh().expiration(),
                jwtProperties.refresh().secret()
        );
    }

    public String extractUsername(String token, boolean refreshToken) {
        return extractClaim(
                token,
                Claims::getSubject,
                refreshToken
        );
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails,
            boolean refreshToken
    ) {
        String username = extractUsername(token, refreshToken);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token, refreshToken);
    }

    private boolean isTokenExpired(String token, boolean refreshToken) {
        return extractExpiration(token, refreshToken)
                .before(new Date());
    }

    private Date extractExpiration(
            String token,
            boolean refreshToken
    ) {
        return extractClaim(
                token,
                Claims::getExpiration,
                refreshToken
        );
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver,
            boolean refreshToken
    ) {
        Claims claims = extractAllClaims(token, refreshToken);

        return resolver.apply(claims);
    }

    private Claims extractAllClaims(
            String token,
            boolean refreshToken
    ) {
        return Jwts.parser()
                .setSigningKey(getSigningKey(refreshToken))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String generateToken(
            Map<String, Object> claims,
            UserDetails userDetails,
            long expiration,
            String secret
    ) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(secret), SignatureAlgorithm.HS512)
                .compact();
    }

    private SecretKey getSigningKey(boolean refreshToken) {
        return refreshToken
                ? getSigningKey(jwtProperties.refresh().secret())
                : getSigningKey(jwtProperties.access().secret());
    }

    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}