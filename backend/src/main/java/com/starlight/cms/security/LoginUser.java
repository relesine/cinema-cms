package com.starlight.cms.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 登录用户上下文对象（从 JWT 解析得到，存入 ThreadLocal 供本次请求使用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {
    private Long userId;
    private String username;
    /** 角色编码集合，如 [ROLE_ADMIN] */
    private List<String> roles;

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
