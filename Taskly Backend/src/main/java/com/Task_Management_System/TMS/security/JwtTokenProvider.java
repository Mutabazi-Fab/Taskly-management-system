package com.Task_Management_System.TMS.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    // HS256 needs a key of at least 256 bits (32 bytes)
    private static final int MIN_SECRET_BYTES = 32;

    private final Key signingKey;
    private final long jwtExpirationInMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationInMs) {
        this.signingKey = buildSigningKey(jwtSecret);
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    private static Key buildSigningKey(String jwtSecret) {
        if (!StringUtils.hasText(jwtSecret)) {
            // Convenient for local development, but every restart signs tokens with a
            // new key, which logs everyone out. Production must set JWT_SECRET.
            log.warn("JWT_SECRET is not set. Using a random signing key; all tokens will be invalidated on restart.");
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }

        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least " + MIN_SECRET_BYTES + " bytes long for HS256 (got " + secretBytes.length + ")");
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(String email, boolean admin) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("admin", admin)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Invalid signature, expired, or malformed JWT token
        }
        return false;
    }
}
