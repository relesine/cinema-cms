package com.starlight.cms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 工具：生成令牌、解析令牌
 * 令牌载荷写入 userId / username / roles（角色编码逗号串）
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${cms.jwt.secret}")
    private String secret;

    @Value("${cms.jwt.expire}")
    private long expire;

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 生成 token */
    public String createToken(Long userId, String username, String roles) {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("roles", roles == null ? "" : roles)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expire))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 解析 token，失败返回 null */
    public Claims parse(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(getKey()).build()
                    .parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.debug("token 解析失败：{}", e.getMessage());
            return null;
        }
    }
}
