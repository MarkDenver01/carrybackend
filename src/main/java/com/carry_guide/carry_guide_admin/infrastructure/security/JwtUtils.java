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

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String getBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (!isStringNullOrEmpty(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // remove bearer prefix
        }
        return null;
    }

    public String generateToken(CustomizedUserDetails customizedUserDetails) {
        String email = customizedUserDetails.getEmail();
        String roles = customizedUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(getSecretKeyProvider())
                .compact();
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail() != null ? user.getEmail() : user.getMobileNumber())
                .claim("role", user.getRole().getRoleState().name())
                .claim("userId", user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS512, getSecretKeyProvider())
                .compact();
    }

    public String generateMobileToken(String mobileNumber) {
        return Jwts.builder()
                .subject(mobileNumber)
                .claim("type", "MOBILE")
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(getSecretKeyProvider())
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSecretKeyProvider())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getMobileFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSecretKeyProvider())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if ("MOBILE".equals(claims.get("type"))) {
                return claims.getSubject(); // mobile number
            }
            return null;
        } catch (JwtException e) {
            logger.error("Failed to extract mobile number: {}", e.getMessage());
            return null;
        }
    }

    private Key getSecretKeyProvider() {
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts
                    .parser()
                    .verifyWith((SecretKey) getSecretKeyProvider())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

}
