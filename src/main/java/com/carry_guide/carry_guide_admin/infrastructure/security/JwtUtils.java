package com.carry_guide.carry_guide_admin.infrastructure.security;

import com.carry_guide.carry_guide_admin.model.entity.User;
import com.carry_guide.carry_guide_admin.service.CustomizedUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import static com.carry_guide.carry_guide_admin.utils.Utility.isStringNullOrEmpty;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String secret;

    @Value("${spring.app.jwtExpirationMs}")
    private int expirationMs;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String getBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (!isStringNullOrEmpty(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // remove bearer prefix
        }
        return null;
    }

    public JwtResponse generateToken(CustomizedUserDetails customizedUserDetails) {
        String email = customizedUserDetails.getEmail();
        String roles = customizedUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSecretKey())
                .compact();

        return new JwtResponse(token, issuedAt, expirationDate);
    }

    public JwtResponse generateMobileToken(String mobileNumber) {
        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(mobileNumber)
                .claim("type", "MOBILE")
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSecretKey())
                .compact();
        return new JwtResponse(token, issuedAt, expirationDate);
    }

    public Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }


    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public long getRemainingValidity(String token) {
        Date now = new Date();
        Date expiration = getExpirationDateFromToken(token);
        long remainingMs = expiration.getTime() - now.getTime();
        return Math.max(remainingMs / 1000, 0);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public static record JwtResponse(
            String token,
            Date issuedAt,
            Date expiresAt
    ) {}

}
