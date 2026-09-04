package com.airesume.analyzer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @jakarta.annotation.PostConstruct
    public void validateJwtSecret() {
        boolean isDevOrTest = activeProfile != null && (activeProfile.contains("dev") || activeProfile.contains("test"));
        if (jwtSecret == null || jwtSecret.isBlank() || "CHANGE_ME_IN_PRODUCTION_JWT_SECRET_KEY_MIN_32_CHARS".equals(jwtSecret)) {
            if (isDevOrTest) {
                this.jwtSecret = "dev_default_secret_key_must_be_at_least_256_bits_long_for_security_compliance_12345";
            } else {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: Insecure or missing JWT_SECRET environment variable in production startup. Set a strong environment variable for JWT_SECRET.");
            }
        } else if (!isDevOrTest && jwtSecret.length() < 32) {
            throw new IllegalStateException("CRITICAL SECURITY ERROR: JWT_SECRET environment variable in production must be at least 256 bits (32+ characters).");
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
