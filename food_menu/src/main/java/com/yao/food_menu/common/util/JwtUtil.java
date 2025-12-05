package com.yao.food_menu.common.util;

import com.yao.food_menu.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token工具类
 * 从配置文件读取密钥，支持敏感信息加密
 */
@Component
public class JwtUtil {

    @Autowired
    private JwtProperties jwtProperties;

    private Key key;
    private long expirationTime;

    /**
     * 初始化密钥
     */
    @PostConstruct
    public void init() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT密钥长度不能小于32个字符，请在配置文件中设置jwt.secret");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationTime = jwtProperties.getExpirationTime();
    }

    /**
     * 生成JWT token
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, null, null);
    }

    /**
     * 生成JWT token（包含家庭和角色信息）
     */
    public String generateToken(Long userId, String username, Long familyId, Integer role) {
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
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT token
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从token中获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从token中获取家庭ID
     */
    public Long getFamilyId(String token) {
        Claims claims = parseToken(token);
        return claims.get("familyId", Long.class);
    }

    /**
     * 从token中获取角色
     */
    public Integer getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", Integer.class);
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
