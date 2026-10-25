package com.starlight.cms.controller.app;

import com.starlight.cms.common.Result;
import com.starlight.cms.security.UserContext;
import com.starlight.cms.service.IBizMemberLevelService;
import com.starlight.cms.service.IBizMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户端 - 会员中心：查看会员信息、开通会员、充值、等级列表
 */
@RestController
@RequestMapping("/app/member")
@RequiredArgsConstructor
public class AppMemberController {

    private final IBizMemberService memberService;
    private final IBizMemberLevelService levelService;

    /** 会员中心数据 */
    @GetMapping("/center")
    public Result<Map<String, Object>> center() {
        return Result.ok(memberService.memberCenter(UserContext.getUserId()));
    }

    /** 开通会员 */
    @PostMapping("/open")
    public Result<Void> open() {
        memberService.openMember(UserContext.getUserId());
        return Result.ok("会员开通成功");
    }

    /** 模拟充值 */
    @PostMapping("/recharge")
    public Result<Void> recharge(@RequestParam BigDecimal amount) {
        memberService.recharge(UserContext.getUserId(), amount);
        return Result.ok("充值成功");
    }

    /** 会员等级说明 */
    @GetMapping("/levels")
    public Result<Object> levels() {
        return Result.ok(levelService.list());
    }
}
