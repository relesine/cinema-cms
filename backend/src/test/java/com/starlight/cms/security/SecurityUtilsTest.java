package com.starlight.cms.security;

import com.starlight.cms.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RBAC 权限工具 + 用户上下文单元测试
 * 覆盖：未登录 401、角色不足 403、员工/管理员放行、ThreadLocal 清理
 */
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("未登录时 requireRole 抛 401")
    void requireRoleWithoutLoginShouldThrow401() {
        BusinessException e = assertThrows(BusinessException.class, () -> SecurityUtils.requireRole("ROLE_USER"));
        assertEquals(401, e.getCode());
    }

    @Test
    @DisplayName("普通用户访问管理员接口抛 403")
    void userCannotAccessAdmin() {
        UserContext.set(new LoginUser(1L, "user01", Collections.singletonList("ROLE_USER")));
        BusinessException e = assertThrows(BusinessException.class, SecurityUtils::requireAdmin);
        assertEquals(403, e.getCode());
    }

    @Test
    @DisplayName("管理员通过 requireAdmin")
    void adminPassesAdminCheck() {
        LoginUser admin = new LoginUser(6L, "admin", Collections.singletonList("ROLE_ADMIN"));
        UserContext.set(admin);
        assertSame(admin, SecurityUtils.requireAdmin());
    }

    @Test
    @DisplayName("员工通过 requireStaff")
    void staffPassesStaffCheck() {
        LoginUser staff = new LoginUser(2L, "staff01", Collections.singletonList("ROLE_STAFF"));
        UserContext.set(staff);
        assertSame(staff, SecurityUtils.requireStaff());
    }

    @Test
    @DisplayName("管理员也通过 requireStaff（管理端可兜底检票）")
    void adminAlsoPassesStaffCheck() {
        UserContext.set(new LoginUser(6L, "admin", Collections.singletonList("ROLE_ADMIN")));
        assertNotNull(SecurityUtils.requireStaff());
    }

    @Test
    @DisplayName("普通用户不通过 requireStaff")
    void userCannotPassStaffCheck() {
        UserContext.set(new LoginUser(1L, "user01", Collections.singletonList("ROLE_USER")));
        BusinessException e = assertThrows(BusinessException.class, SecurityUtils::requireStaff);
        assertEquals(403, e.getCode());
    }

    @Test
    @DisplayName("UserContext 清理后取不到用户（防线程复用串号）")
    void clearShouldRemoveThreadLocal() {
        UserContext.set(new LoginUser(1L, "user01", Arrays.asList("ROLE_USER")));
        assertEquals(1L, UserContext.getUserId());
        assertEquals("user01", UserContext.getUsername());
        UserContext.clear();
        assertNull(UserContext.get());
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
    }
}
