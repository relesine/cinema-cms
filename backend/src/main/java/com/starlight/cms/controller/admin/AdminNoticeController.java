package com.starlight.cms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starlight.cms.common.Constants;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.BizNotice;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IBizNoticeService;
import com.starlight.cms.util.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 管理后台 - 公告管理
 */
@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final IBizNoticeService noticeService;
    private final RedisService redisService;

    @GetMapping("/page")
    public Result<Page<BizNotice>> page(@RequestParam(defaultValue = "1") long current,
                                        @RequestParam(defaultValue = "10") long size) {
        SecurityUtils.requireAdmin();
        return Result.ok(noticeService.page(new Page<>(current, size),
                new LambdaQueryWrapper<BizNotice>().orderByDesc(BizNotice::getPublishTime)));
    }

    /** 新增/修改公告；状态为发布时补发布时间 */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody BizNotice notice) {
        SecurityUtils.requireAdmin();
        if (notice.getStatus() != null && notice.getStatus() == 1 && notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
        }
        noticeService.saveOrUpdate(notice);
        redisService.delete(Constants.REDIS_NOTICE);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SecurityUtils.requireAdmin();
        noticeService.removeById(id);
        redisService.delete(Constants.REDIS_NOTICE);
        return Result.ok();
    }
}
