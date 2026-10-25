package com.starlight.cms.aspect;

import cn.hutool.json.JSONUtil;
import com.starlight.cms.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作审计切面
 * 拦截全部控制器的“写操作”（POST/PUT/DELETE），以单行 JSON 记录到 logs/audit.log：
 *   操作人、角色、IP、模块、动作、请求参数（密码自动脱敏）、耗时、结果
 * 攻防用途：出错追溯、越权排查、敏感操作留痕。
 */
@Slf4j
@Aspect
@Component
public class OperateLogAspect {

    /** 审计专用 logger（logback 中指向 audit.log 通道） */
    private static final Logger AUDIT = LoggerFactory.getLogger("OPERATE_AUDIT");

    /** 需要脱敏的参数字段名 */
    private static final String[] SENSITIVE_FIELDS = {"password", "oldPassword", "newPassword", "confirmPassword"};

    /** 控制器写操作切点：common/app/staff/admin 全部控制器 */
    @Around("execution(* com.starlight.cms.controller..*.*(..))")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs == null ? null : attrs.getRequest();
        // 只审计写操作；GET/无请求上下文（如定时任务、启动）直接放行
        if (request == null || "GET".equalsIgnoreCase(request.getMethod()) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return joinPoint.proceed();
        }

        long start = System.currentTimeMillis();
        String error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            error = e.getClass().getSimpleName() + ": " + e.getMessage();
            throw e;
        } finally {
            writeAudit(joinPoint, request, System.currentTimeMillis() - start, error);
        }
    }

    /** 组装并写出一条审计记录（不抛出、不影响业务） */
    private void writeAudit(ProceedingJoinPoint joinPoint, HttpServletRequest request, long cost, String error) {
        try {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("time", java.time.LocalDateTime.now().toString());
            record.put("module", joinPoint.getSignature().getDeclaringTypeName()
                    .substring(joinPoint.getSignature().getDeclaringTypeName().lastIndexOf('.') + 1));
            record.put("action", joinPoint.getSignature().getName());
            record.put("uri", request.getRequestURI());
            record.put("method", request.getMethod());
            record.put("ip", resolveIp(request));
            record.put("operator", UserContext.getUsername() == null ? "anonymous" : UserContext.getUsername());
            record.put("params", maskArgs(joinPoint.getArgs()));
            record.put("cost", cost + "ms");
            record.put("success", error == null);
            if (error != null) {
                record.put("error", error);
            }
            AUDIT.info(JSONUtil.toJsonStr(record));
        } catch (Exception e) {
            log.warn("操作审计写入失败：{}", e.getMessage());
        }
    }

    /** 参数序列化：敏感字段脱敏，参数过大截断 */
    private String maskArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null || arg instanceof javax.servlet.ServletRequest || arg instanceof javax.servlet.ServletResponse
                    || arg instanceof org.springframework.web.multipart.MultipartFile) {
                continue;
            }
            String json = JSONUtil.toJsonStr(arg);
            // 脱敏：把敏感字段值替换为 ***
            for (String field : SENSITIVE_FIELDS) {
                json = json.replaceAll("(\"" + field + "\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"");
            }
            if (json.length() > 1000) {
                json = json.substring(0, 1000) + "...(truncate)";
            }
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append(json);
        }
        return sb.append('}').toString();
    }

    /** 取真实 IP：优先 X-Forwarded-For 第一跳，其次 X-Real-IP，最后 remoteAddr */
    private String resolveIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int idx = ip.indexOf(',');
            return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
