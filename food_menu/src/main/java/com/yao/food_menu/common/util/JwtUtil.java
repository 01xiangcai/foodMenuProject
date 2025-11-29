package com.yao.food_menu.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token utility
 */
public class JwtUtil {

    // Secret key for JWT (should be stored in configuration in production)
    private static final String SECRET = "YourSecretKeyForJWTTokenGenerationMustBeLongEnough";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Token expiration time: 7 days
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * Generate JWT token
     */
    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, null, null);
    }

    /**
     * Generate JWT token with family and role info
     */
    public static String generateToken(Long userId, String username, Long familyId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        if (familyId != null) {
            claims.put("familyId", familyId);
        }

        if (role != null) {
            claims.put("role", role);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parse JWT token
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get user ID from token
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Get username from token
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Get family ID from token
     */
    public static Long getFamilyId(String token) {
        Claims claims = parseToken(token);
        return claims.get("familyId", Long.class);
    }

    /**
     * Get user role from token
     */
    public static Integer getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", Integer.class);
    }

    /**
     * Validate token
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
