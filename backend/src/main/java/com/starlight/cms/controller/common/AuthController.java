package com.starlight.cms.controller.common;

import com.starlight.cms.common.Constants;
import com.starlight.cms.common.Result;
import com.starlight.cms.dto.LoginDTO;
import com.starlight.cms.dto.RegisterDTO;
import com.starlight.cms.entity.SysUser;
import com.starlight.cms.security.JwtUtils;
import com.starlight.cms.security.UserContext;
import com.starlight.cms.service.ISysUserService;
import com.starlight.cms.util.RedisService;
import com.starlight.cms.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器：登录、注册、当前用户信息、退出
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ISysUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;

    @Value("${cms.jwt.expire}")
    private long expire;

    /** 登录（防爆破——连续失败 5 次锁定 10 分钟） */
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO dto) {
        String username = dto.getUsername();
        String failKey = Constants.REDIS_LOGIN_FAIL + username;
        String lockKey = Constants.REDIS_LOGIN_LOCK + username;
        // 1.锁定中直接拒绝（不暴露是否存在此账号）
        if (Boolean.TRUE.equals(redisService.exists(lockKey))) {
            return Result.error(1002, "连续登录失败次数过多，账号已锁定10分钟，请稍后再试");
        }
        SysUser user = userService.findByUsername(username);
        // 用户不存在或密码错误统一提示，避免泄露账号是否存在；同时计入失败次数
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return countLoginFail(failKey, lockKey, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error(1003, "账号已被禁用，请联系管理员");
        }
        // 登录成功：清除失败计数
        redisService.delete(failKey);
        List<String> roles = userService.getRoleCodes(user.getId());
        String token = jwtUtils.createToken(user.getId(), user.getUsername(), String.join(",", roles));
        // 登录态写 Redis，登出时删除即可让令牌立即失效
        redisTemplate.opsForValue().set(Constants.REDIS_TOKEN + token, user.getId(),
                expire, TimeUnit.MILLISECONDS);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(roles);
        // 主角色：管理员优先，其次员工，最后普通用户
        if (roles.contains(Constants.ROLE_ADMIN)) {
            vo.setPrimaryRole(Constants.ROLE_ADMIN);
        } else if (roles.contains(Constants.ROLE_STAFF)) {
            vo.setPrimaryRole(Constants.ROLE_STAFF);
        } else {
            vo.setPrimaryRole(Constants.ROLE_USER);
        }
        return Result.ok("登录成功", vo);
    }

    /** 注册（默认普通用户角色） */
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterDTO dto) {
        if (userService.findByUsername(dto.getUsername()) != null) {
            return Result.error(1004, "账号已存在，请更换");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // service 内会加密
        user.setNickname(dto.getNickname() == null ? dto.getUsername() : dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setGender(0);
        user.setStatus(1);
        userService.addUserWithRole(user, 1L); // 1=普通用户角色
        return Result.ok();
    }

    /** 获取当前登录用户信息 + 角色 + 菜单（管理端用） */
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = UserContext.getUserId();
        SysUser user = userService.getById(userId);
        user.setPassword(null);
        List<String> roles = userService.getRoleCodes(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("roles", roles);
        return Result.ok(map);
    }

    /** 退出登录：删除 Redis 登录态 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Object token = request.getAttribute("currentToken");
        if (token != null) {
            redisTemplate.delete(Constants.REDIS_TOKEN + token);
        }
        return Result.ok();
    }

    /** 登录失败计数：记录一次失败，达到阈值则锁定（返回失败提示） */
    private Result<LoginVO> countLoginFail(String failKey, String lockKey, String message) {
        try {
            Object count = redisService.get(failKey);
            int next = count == null ? 1 : Integer.parseInt(count.toString()) + 1;
            if (next >= Constants.LOGIN_FAIL_LIMIT) {
                redisService.set(lockKey, next, Constants.LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
                redisService.delete(failKey);
                return Result.error(1002, "连续登录失败次数过多，账号已锁定10分钟，请稍后再试");
            }
            redisService.set(failKey, next, Constants.LOGIN_FAIL_MINUTES, TimeUnit.MINUTES);
        } catch (NumberFormatException ignored) {
            // 计数异常不阻断登录流程，按原提示返回
        }
        return Result.error(1001, message);
    }
}
