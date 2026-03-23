package com.karma.platform.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final String issuer;
    private final long accessMinutes;
    private final long refreshDays;

    public JwtService(
            @Value("${karma.jwt.secret}") String secret,
            @Value("${karma.jwt.issuer}") String issuer,
            @Value("${karma.jwt.access-token-minutes}") long accessMinutes,
            @Value("${karma.jwt.refresh-token-days}") long refreshDays
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessMinutes = accessMinutes;
        this.refreshDays = refreshDays;
    }

    public String createAccessToken(String userId, String email, String role) {
        return buildToken(userId, email, role, Instant.now().plus(accessMinutes, ChronoUnit.MINUTES), "access");
    }

    public String createRefreshToken(String userId, String email, String role) {
        return buildToken(userId, email, role, Instant.now().plus(refreshDays, ChronoUnit.DAYS), "refresh");
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(secretKey).requireIssuer(issuer).build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(String userId, String email, String role, Instant expiresAt, String type) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(userId)
                .claims(Map.of("email", email, "role", role, "type", type))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
}
