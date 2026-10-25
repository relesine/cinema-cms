package com.starlight.cms.controller.staff;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.SysUser;
import com.starlight.cms.mapper.SysUserMapper;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IBizMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 员工端 - 会员办理：查询用户、开卡、充值、会员列表
 */
@RestController
@RequestMapping("/staff/member")
@RequiredArgsConstructor
public class StaffMemberController {

    private final IBizMemberService memberService;
    private final SysUserMapper userMapper;

    /** 按账号/昵称/手机号搜索用户（办理前先找到用户） */
    @GetMapping("/search-user")
    public Result<List<SysUser>> searchUser(@RequestParam String keyword) {
        SecurityUtils.requireStaff();
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .and(w -> w.like(SysUser::getUsername, keyword)
                        .or().like(SysUser::getNickname, keyword)
                        .or().like(SysUser::getPhone, keyword))
                .last("limit 20"));
        users.forEach(u -> u.setPassword(null));
        return Result.ok(users);
    }

    /** 为指定用户开通会员 */
    @PostMapping("/open/{userId}")
    public Result<Void> open(@PathVariable Long userId) {
        SecurityUtils.requireStaff();
        memberService.openMember(userId);
        return Result.ok("开卡成功");
    }

    /** 为指定用户充值 */
    @PostMapping("/recharge")
    public Result<Void> recharge(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        SecurityUtils.requireStaff();
        memberService.recharge(userId, amount);
        return Result.ok("充值成功");
    }

    /** 会员列表 */
    @GetMapping("/page")
    public Result<IPage<Map<String, Object>>> page(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) String keyword) {
        SecurityUtils.requireStaff();
        return Result.ok(memberService.adminPage(current, size, keyword));
    }
}
