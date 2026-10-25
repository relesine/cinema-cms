package com.starlight.cms.security;

/**
 * 当前登录用户上下文：基于 ThreadLocal，在一次请求内随时获取当前用户
 * 请求结束必须 clear，避免线程池复用导致用户串号
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getUserId();
    }

    public static String getUsername() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getUsername();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
