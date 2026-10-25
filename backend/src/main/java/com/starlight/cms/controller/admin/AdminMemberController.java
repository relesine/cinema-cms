package com.starlight.cms.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starlight.cms.common.Result;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IBizMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 管理后台 - 会员管理（只读 + 员工端可办理） */
@RestController
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class AdminMemberController {

    private final IBizMemberService memberService;

    @GetMapping("/page")
    public Result<IPage<Map<String, Object>>> page(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) String keyword) {
        SecurityUtils.requireAdmin();
        return Result.ok(memberService.adminPage(current, size, keyword));
    }
}
