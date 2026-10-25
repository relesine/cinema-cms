package com.starlight.cms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.SysRole;
import com.starlight.cms.entity.SysUser;
import com.starlight.cms.mapper.SysRoleMapper;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 用户/员工/权限管理
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final ISysUserService userService;
    private final SysRoleMapper roleMapper;

    /** 用户分页（可按账号/昵称/手机号搜索） */
    @GetMapping("/page")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") long current,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword) {
        SecurityUtils.requireAdmin();
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        w.orderByAsc(SysUser::getId);
        Page<SysUser> page = userService.page(new Page<>(current, size), w);
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(page);
    }

    /** 角色列表 */
    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        SecurityUtils.requireAdmin();
        return Result.ok(roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId)));
    }

    /** 查某用户的角色编码 */
    @GetMapping("/role-codes/{userId}")
    public Result<Object> roleCodes(@PathVariable Long userId) {
        SecurityUtils.requireAdmin();
        return Result.ok(userService.getRoleCodes(userId));
    }

    /** 新增用户/员工，body: {user: SysUser, roleId: Long} */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody Map<String, Object> body) {
        SecurityUtils.requireAdmin();
        // 简单解析：前端传 username/password/nickname/phone 与 roleId
        SysUser user = new SysUser();
        user.setUsername((String) body.get("username"));
        user.setPassword((String) body.get("password"));
        user.setNickname((String) body.get("nickname"));
        user.setRealName((String) body.get("realName"));
        user.setPhone((String) body.get("phone"));
        user.setGender(body.get("gender") == null ? 0 : Integer.valueOf(body.get("gender").toString()));
        user.setStatus(1);
        Long roleId = body.get("roleId") == null ? 1L : Long.valueOf(body.get("roleId").toString());
        if (user.getUsername() == null || user.getPassword() == null) {
            return Result.error("账号和密码不能为空");
        }
        if (userService.findByUsername(user.getUsername()) != null) {
            return Result.error("账号已存在");
        }
        userService.addUserWithRole(user, roleId);
        return Result.ok();
    }

    /** 启用/禁用 */
    @PostMapping("/status")
    public Result<Void> status(@RequestParam Long id, @RequestParam Integer status) {
        SecurityUtils.requireAdmin();
        SysUser u = new SysUser();
        u.setId(id);
        u.setStatus(status);
        userService.updateById(u);
        return Result.ok();
    }

    /** 重置密码 */
    @PostMapping("/reset-pwd")
    public Result<Void> resetPwd(@RequestParam Long id, @RequestParam String password) {
        SecurityUtils.requireAdmin();
        userService.resetPassword(id, password);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SecurityUtils.requireAdmin();
        if (id == 1L) {
            return Result.error("内置超级管理员不可删除");
        }
        userService.removeById(id);
        return Result.ok();
    }
}
