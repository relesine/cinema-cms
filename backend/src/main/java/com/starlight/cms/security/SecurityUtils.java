package com.starlight.cms.security;

import com.starlight.cms.exception.BusinessException;

/**
 * 接口权限校验小工具：基于当前登录用户的角色做 RBAC 校验
 */
public class SecurityUtils {

    private SecurityUtils() {}

    /** 当前用户必须拥有指定角色之一，否则抛 403 */
    public static LoginUser requireRole(String... roles) {
        LoginUser user = UserContext.get();
        if (user == null) {
            throw new BusinessException(401, "未登录，请先登录");
        }
        for (String role : roles) {
            if (user.hasRole(role)) {
                return user;
            }
        }
        throw new BusinessException(403, "没有访问权限");
    }

    /** 仅管理员 */
    public static LoginUser requireAdmin() {
        return requireRole("ROLE_ADMIN");
    }

    /** 员工或管理员 */
    public static LoginUser requireStaff() {
        return requireRole("ROLE_STAFF", "ROLE_ADMIN");
    }
}
