package com.starlight.cms.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具单元测试
 * 覆盖：生成/解析往返、角色为空、过期令牌、篡改令牌、密钥不匹配
 */
class JwtUtilsTest {

    private static final String SECRET = "unit-test-secret-key-must-be-long-enough-0123456789";

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expire", 86_400_000L);
    }

    @Test
    @DisplayName("生成后解析：userId/username/roles 全部还原")
    void createAndParseShouldKeepClaims() {
        String token = jwtUtils.createToken(7L, "admin", "ROLE_ADMIN");
        Claims claims = jwtUtils.parse(token);

        assertNotNull(claims);
        assertEquals(7L, ((Number) claims.get("userId")).longValue());
        assertEquals("admin", claims.get("username"));
        assertEquals("ROLE_ADMIN", claims.get("roles"));
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("roles 传 null 时写入空串，解析不为 null")
    void nullRolesShouldBecomeEmptyString() {
        Claims claims = jwtUtils.parse(jwtUtils.createToken(1L, "user01", null));
        assertNotNull(claims);
        assertEquals("", claims.get("roles"));
    }

    @Test
    @DisplayName("过期令牌解析返回 null")
    void expiredTokenShouldReturnNull() {
        ReflectionTestUtils.setField(jwtUtils, "expire", -1_000L);
        String expired = jwtUtils.createToken(1L, "user01", "ROLE_USER");
        assertNull(jwtUtils.parse(expired));
    }

    @Test
    @DisplayName("被篡改的令牌解析返回 null")
    void tamperedTokenShouldReturnNull() {
        String token = jwtUtils.createToken(1L, "user01", "ROLE_USER");
        assertNull(jwtUtils.parse(token + "x"));
    }

    @Test
    @DisplayName("更换密钥后旧令牌解析失败（签名校验生效）")
    void tokenFromAnotherSecretShouldReturnNull() {
        String token = jwtUtils.createToken(1L, "user01", "ROLE_USER");
        ReflectionTestUtils.setField(jwtUtils, "secret", "another-secret-key-also-long-enough-9876543210abcd");
        assertNull(jwtUtils.parse(token));
    }
}
