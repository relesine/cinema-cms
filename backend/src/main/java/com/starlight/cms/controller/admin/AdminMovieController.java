package com.starlight.cms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starlight.cms.common.Constants;
import com.starlight.cms.common.Result;
import com.starlight.cms.entity.BizMovie;
import com.starlight.cms.security.SecurityUtils;
import com.starlight.cms.service.IBizMovieService;
import com.starlight.cms.util.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 电影管理
 */
@RestController
@RequestMapping("/admin/movie")
@RequiredArgsConstructor
public class AdminMovieController {

    private final IBizMovieService movieService;
    private final RedisService redisService;

    @GetMapping("/page")
    public Result<Page<BizMovie>> page(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer movieStatus) {
        SecurityUtils.requireAdmin();
        LambdaQueryWrapper<BizMovie> w = new LambdaQueryWrapper<>();
        w.like(keyword != null && !keyword.isBlank(), BizMovie::getName, keyword)
                .eq(movieStatus != null, BizMovie::getMovieStatus, movieStatus)
                .orderByDesc(BizMovie::getId);
        return Result.ok(movieService.page(new Page<>(current, size), w));
    }

    /** 全部电影（排片页下拉用） */
    @GetMapping("/all")
    public Result<List<BizMovie>> all() {
        SecurityUtils.requireAdmin();
        return Result.ok(movieService.list(new LambdaQueryWrapper<BizMovie>().orderByAsc(BizMovie::getId)));
    }

    @GetMapping("/{id}")
    public Result<BizMovie> detail(@PathVariable Long id) {
        SecurityUtils.requireAdmin();
        return Result.ok(movieService.getById(id));
    }

    /** 新增/修改（有 id 为修改） */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody BizMovie movie) {
        SecurityUtils.requireAdmin();
        movieService.saveOrUpdate(movie);
        redisService.delete(Constants.REDIS_MOVIE_HOT);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SecurityUtils.requireAdmin();
        movieService.removeById(id);
        redisService.delete(Constants.REDIS_MOVIE_HOT);
        return Result.ok();
    }
}
