package com.starlight.cms.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starlight.cms.common.Constants;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.BizMovie;
import com.starlight.cms.service.IBizMovieService;
import com.starlight.cms.util.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 电影浏览（列表/详情/热映，部分接口在 WebMvcConfig 中放行无需登录）
 */
@RestController
@RequestMapping("/app/movie")
@RequiredArgsConstructor
public class AppMovieController {

    private final IBizMovieService movieService;
    private final RedisService redisService;

    /** 电影列表：movieStatus 1热映 0待映；可按类型/关键词过滤 */
    @GetMapping("/list")
    public Result<Page<BizMovie>> list(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) Integer movieStatus,
                                       @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizMovie> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(movieStatus != null, BizMovie::getMovieStatus, movieStatus)
                .like(keyword != null && !keyword.isBlank(), BizMovie::getName, keyword)
                .orderByDesc(BizMovie::getScore).orderByAsc(BizMovie::getId);
        return Result.ok(movieService.page(new Page<>(current, size), wrapper));
    }

    /** 电影详情 */
    @GetMapping("/detail/{id}")
    public Result<BizMovie> detail(@PathVariable Long id) {
        return Result.ok(movieService.getById(id));
    }

    /** 首页热映（走 Redis 缓存演示热点数据） */
    @GetMapping("/hot")
    @SuppressWarnings("unchecked")
    public Result<List<BizMovie>> hot() {
        Object cache = redisService.get(Constants.REDIS_MOVIE_HOT);
        if (cache instanceof List) {
            return Result.ok((List<BizMovie>) cache);
        }
        List<BizMovie> hotList = movieService.list(new LambdaQueryWrapper<BizMovie>()
                .eq(BizMovie::getMovieStatus, 1)
                .orderByDesc(BizMovie::getScore).last("limit 6"));
        redisService.set(Constants.REDIS_MOVIE_HOT, hotList, 30, java.util.concurrent.TimeUnit.MINUTES);
        return Result.ok(hotList);
    }
}
