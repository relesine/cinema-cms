package com.starlight.cms.security;

import com.starlight.cms.common.Constants;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * JWT 登录拦截器：
 * 1. 取请求头 token 并解析
 * 2. 校验 Redis 中 token 仍有效（登出即删除，实现主动下线）
 * 3. 把登录用户放入 UserContext
 * 具体哪些路径需要拦截在 WebMvcConfig 中配置
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行浏览器跨域预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!StringUtils.hasText(token)) {
            return writeUnauthorized(response, "未登录或登录已过期，请先登录");
        }
        Claims claims = jwtUtils.parse(token);
        if (claims == null) {
            return writeUnauthorized(response, "登录令牌无效，请重新登录");
        }
        // Redis 校验：登录时写入、登出时删除
        Boolean exists = redisTemplate.hasKey(Constants.REDIS_TOKEN + token);
        if (Boolean.FALSE.equals(exists)) {
            return writeUnauthorized(response, "登录状态已失效，请重新登录");
        }
        Long userId = claims.get("userId", Number.class).longValue();
        String username = claims.get("username", String.class);
        String rolesStr = claims.get("roles", String.class);
        List<String> roles = StringUtils.hasText(rolesStr)
                ? Arrays.asList(rolesStr.split(",")) : Collections.emptyList();
        UserContext.set(new LoginUser(userId, username, roles));
        // 把原始 token 也挂到请求属性上，便于控制器做登出
        request.setAttribute("currentToken", token);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /** 直接写出 401 JSON（避免被重定向到登录页） */
    private boolean writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"code\":401,\"message\":\"" + msg + "\",\"data\":null}");
            writer.flush();
        }
        return false;
    }
}
