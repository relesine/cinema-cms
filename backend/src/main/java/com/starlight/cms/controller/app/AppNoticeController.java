package com.starlight.cms.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starlight.cms.common.Constants;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.BizNotice;
import com.starlight.cms.service.IBizNoticeService;
import com.starlight.cms.util.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户端 - 公告（列表放行匿名访问，走 Redis 缓存）
 */
@RestController
@RequestMapping("/app/notice")
@RequiredArgsConstructor
public class AppNoticeController {

    private final IBizNoticeService noticeService;
    private final RedisService redisService;

    @GetMapping("/list")
    @SuppressWarnings("unchecked")
    public Result<List<BizNotice>> list() {
        Object cache = redisService.get(Constants.REDIS_NOTICE);
        if (cache instanceof List) {
            return Result.ok((List<BizNotice>) cache);
        }
        List<BizNotice> list = noticeService.list(new LambdaQueryWrapper<BizNotice>()
                .eq(BizNotice::getStatus, 1)
                .orderByDesc(BizNotice::getPublishTime));
        redisService.set(Constants.REDIS_NOTICE, list, 10, TimeUnit.MINUTES);
        return Result.ok(list);
    }
}
