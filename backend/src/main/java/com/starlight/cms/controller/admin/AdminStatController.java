package com.starlight.cms.controller.admin;

import com.starlight.cms.common.Result;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台 - 票房数据大盘
 */
@RestController
@RequestMapping("/admin/stat")
@RequiredArgsConstructor
public class AdminStatController {

    private final IStatService statService;

    /** 核心指标 */
    @GetMapping("/overview")
    public Result<Object> overview() {
        SecurityUtils.requireAdmin();
        return Result.ok(statService.overview());
    }

    /** 电影票房排行 */
    @GetMapping("/movie-rank")
    public Result<Object> movieRank() {
        SecurityUtils.requireAdmin();
        return Result.ok(statService.movieRank());
    }

    /** 近 N 天票房趋势 */
    @GetMapping("/trend")
    public Result<Object> trend(@RequestParam(defaultValue = "7") int days) {
        SecurityUtils.requireAdmin();
        return Result.ok(statService.salesTrend(days));
    }
}
